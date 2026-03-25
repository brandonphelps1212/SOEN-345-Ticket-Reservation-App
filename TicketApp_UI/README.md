# SOEN 345 – Android Frontend (Kourosh – Member 4)

## Screens built
| Screen | File | Description |
|--------|------|-------------|
| Registration | `RegisterActivity` | Register by email or phone, mirrors `UserService` validation |
| Event List | `EventListActivity` | Browse all ACTIVE events from Firebase |
| Search & Filter | Built into EventListActivity | Filter by category + location, mirrors `EventService.search()` |
| Event Detail | `EventDetailActivity` | Full event info + Book Now button |
| Booking Confirmation | `BookingConfirmationActivity` | Shows reservation ID, ticket ID, amount paid |

## Project structure
```
app/src/main/java/com/soen345/ticketReservation/
├── model/
│   ├── User.java           ← mirrors backend User.java
│   ├── Event.java          ← mirrors backend Event.java
│   └── Reservation.java    ← mirrors backend Reservation.java
├── service/
│   └── FirebaseRepository.java  ← all Firebase reads/writes
├── adapter/
│   └── EventAdapter.java   ← RecyclerView adapter for event list
└── ui/
    ├── MainActivity.java           ← entry point / router
    ├── register/RegisterActivity
    ├── eventlist/EventListActivity
    ├── eventdetail/EventDetailActivity
    └── booking/BookingConfirmationActivity
```

## ⚠️ BEFORE YOU RUN — Firebase setup (2 steps)

### Step 1 – Get google-services.json from Yan
Ask Yan to download it from:
> Firebase Console → Project Settings → General → Your Apps → Download google-services.json

Then **replace** `app/google-services.json` with the real file.

### Step 2 – Register this app in the Firebase Console
The package name is: `com.soen345.ticketReservation`

In Firebase Console → Project Settings → Add App → Android
Enter the package name above and download the new `google-services.json`.

## How to open in Android Studio
1. Open Android Studio
2. File → Open → select the `TicketApp` folder
3. Wait for Gradle sync to finish
4. Replace `app/google-services.json` with Yan's real file
5. Run on emulator or device (API 26+)

## How Firebase data flows
- **Registration** → writes to `users/{userId}`
- **Event List** → reads from `events/` (ACTIVE only)
- **Booking** → writes to `reservations/{reservationId}`, decrements `events/{id}/availableSeats`

All collection paths match the backend `firebase-schema.json` exactly.
