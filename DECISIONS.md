# Decisions


This file explains what choices I made and why for this SOS alert system as per the requirement given to me.
---

## 1. System design

I have implemented a standard Spring layering desinged in four layers: controller --> service (interface) --> serviceImpl
--> repository. Controllers do no core logic and do not access the respository instead they validate the DTO, call one service
method, and wrap the result in the `ApiResponse` format. All the logic is implemented in ServiceImpl classes.

The following reflects how a request flow from alert intake to the rescue dispatch.

1. `POST /devices/{id}/alerts` — Takes a SosAlertRequest and creates the alert.
2. The service function decides if the request is the genuine emergency alert or the retransmission in a small interval of time i.e.
   **retransmission or new alert**, and either folds the alert in the existing alert with retransmissionCount increased by 1 or create a new alert based on the ALERT_DEDUP_WINDOW_MINUTES set in the env. Every signal duplicate or the real emergency signal create a record in `alert_signal`.
3. If the new alert is created, it resolve where the alert is coming from through the device_assignment to order and tags the orderId to the alert.
4. A coordinator sees it via `GET /alerts?status=OPEN`, opens alert and claims it.
5. If nobody claims it, the EscalationScheduler escalates the alert after the certain time window.
6. The alert can be closed by the `/resolve`.


**Scaling to hundreds of devices**: Scaling could effect the database operation with flooded requests. I would first add the app instances and increase the database capacity. Simultaneously, I will run a load test to find out the bottelneck and improve the database operations.

---

## 2. Data model

Seven enities has been created which are `device`, `trek_order`, `trekker`, `device_assignment`, `alert`,
`alert_signal`, `coordinator`.

**Device**: A device holds the devices serial_number and metadata. Here no device is directly linked to the order or trekker instead the device and order link is established by the device_assignment table.

**TrekOrder**: A order entity stores the orderRef, trekname, start_date, end_date and list of the trekker. The count of Trekker represents if the order is solo or a group.

**Trekker**: This simply stores the information of the person joining the trek (Name, Phone).

**DeviceAssignment**: This entity links the order with the device. A device is assigned to a order in a certain time frame which is recorded by this table so that the device assignment history can be maintained. 

**An order can have multiple device assigned to it.**
**A device can only be assigned t one order at a certain time, but can be assigned to multiple order over time.**
**Device can be reassigned to other TrekOrder in the middle of time. This close the previous assignment with endReason = "REASSIGNED".**
**A Device assignment can be closed when the trek is cancelled or completed.**

**This design helps to record which device served which order over time and prevent the overwrite on the device if the device is reassigned.**

**Alert is represented by two tables Alert and AlertSignal**

**Alert**: This stores the DeviceId, OrderId, alert status('OPEN', 'ESCALATED','CLAIMED','RESOLVED'), retrasmissionCount, signalDetails, and metadata. 

**AlertSignal**: This stores the AlertId, signalDetails, SignalKind('RAISED','RETRANSMISSION') etc.

The alert request from the device either creates a new alert row or folds in the existing row based on the genuinity of the alert resolved by the ALERT_DEDUP_WINDOW_TIME. This prevent the creation of retransmitted signal into the alert table which does not mislead the responds with the duplicate alert while other genuine alert might go un-attended.

The alert_signal table is created to store the retransmission trail from the alert raised_time so that the retrasmission close-in time moment of the trekker or group can observed.

**Coordinator**: A coordinator can claim the alert if that alert is unattended. It mainly stores the coordinator name for now.

---

## 3. Device ambiguity

The question of which Trekorder the alert belongs to is ressolved by the deviceAssignment window.

When a new alert is raised, `resolveOrder()` checks for the assignments whose window contains the moment the SOS
was raised (`assigned_from <= at AND (assigned_to IS NULL OR assigned_to > at)`), and:

- **exactly one match** → that is the order attached to that device.
- **zero** → leave order = null.

An alert with no order attached still gets raised, and gets claimed. It retuen with null order. A coordinator can resolve it with
`POST /alerts/{id}/assign-order`. For an emergency system, order=null is better than a
confident wrong response.

**Assumption:** that device_assignment windows are accurate and non-overlapping, so that the check which assignment window the Alert was raised give one record. Also, here i have assumed that device is assigned to the order when the alert comes to the system but 

**What breaks if that's wrong:** if there is a overlapping window then it makes the query return two rows,
and the alert comes back unattributed. So the coordinator has to attribute the order for that alert by hand.

Once the alert points at an order, we can easily fetch the order details with the trekkers details from `GET /alerts/{id}`.

---

## 4. Deduplication

**The rule.** For every Sos signal request, the AlertService.ingest() function look at the device's most recent alert. If it is not resolved and the new
signal is within the dedup window of the alert's last_signal, it considers the signal as retransmission and folds the signal into the existing alert record but keeps the record or the retransmission in the AlertSignal table for the reference.

I think 5 minutes is suitable for the alert deduplication window. A satellite device can send a brust of singnals in one press due to bad connectivity which may last 2-3 minutes. Likewise, if the window is set to be longer than the second genuine alert can go unacknowledge as it might flod into the existing alert. Hence, 5 minutes window is suitable as it provides the exact window for the retransmission from the device.

The distance parameter is not included in the retransmission check.

---

## 5. Concurrency — what actually stops a double claim

The concurrency check to stop two coordinators to claim the same alert is done through the compare and swap approach with the guarded update, that makes the udpate of the alert status in a single query by checking the status on the instance of making the write operation.

The status check is inside the write. For an UPDATE operation, Postgres explicitly locks the row-level write.
Hence, for claiming the alert, the row is locked for the duration of the UPDATE, so multiple concurrent claims are taken, but the first that flips the OPEN-->CLAIMED goes through with row update, and rest checks the alert status if that has the status ('OPEN' OR 'ESCALATED') against the same alert and matchs nothing and no row is changed, throws the exception.

**Concurrency Check**
For the concurrency check of the multiple coordinator claims, i ran a test container against the real postgres. The `AlertClaimConcurrencyTest` fires the 20 real claim requests at one alert by using the countDownLatch so that the threads actually collide. The result is logged where the exactly one claim sucessfully update the row and other throws the 409 Conflict error.
---

## 6. Escalation

If an alert is not answered by anybody can be regarded as the failure of the system, hence those alert not claimed for long time should be flagged, so that the coordinator do not forgot to respond to those alerts.

I have built a AlertEscalationScheduler that runs every 60 seconds(can be altered thourgh env variables) and moves any alert still OPEN which is past the threshold(also defined in the env) to ESCALATED. An escalated alert is still claimable, but it is flagged as urgent.

Here the clock runs compares the alert witht he threshold from `raised_at` and not `last_signal_at` because if the retransmission signal is flooded or desperate trekker re-presses then the alert's escalation is delayed if it is compared to `last_signal_at`. Hence, the `raised_at` is the suitable comparison ground for this.

The escalation status update is guarded by the UPDATE where the postgres locks the row so that the concurrent escalation change and claim request does not conflict with each other.

Normally, 15 minutes could be suitable for the escalation scheduler to run since that is the long enough for the coordinator to claim the alert within that window.

---

## 7. Tradeoffs

**Simplicity**

-For simplicity, i have used the same response envelope in all the response. 

-Likewise, i have not integrated any security related feature like login, authorization, method level authorization and all.

-Also, the escalation is notified through logging only, no any notification system is integrated.


**Robustness**

- The deduplication check is maintained since this is a emergency system and  the duplicate data should not mislead the rescuer.

- Also, i have used the flyway migration because it provides a clean and two- way approach for the database where we can track how the database has evolved.

- In the concurrency test through TestContainer, i have used the real postgres image to simulate a concurrent claims on a single alert.

## 8. What I'm unsure about


- The alert ingest the client supplied timestamp, and trusts the device's own clock. A device with incorrect clock could set the wrong date/time of the alert. We could use the server receive time also, but sometimes the signal trasmission could run late and the server received time can delay the escalation.

- A claimed alert but un-resolved for the long time could be one of the weak link here. A claimed alert never goes through the escalation, so this could create a issue in emergency case if the coordinator claims and goes quiet.

- The escalation is maintained but the alert fetching is not sorted according to the priority level of the status.

- Another weak point is that, the alert can be set to RESOLVED without ever being CLAIMED, so i am not sure about how should the alert resolution flow look like.

