# PHED Kohistan Upper — Official Complaint System

## Included
- Android app targeting API 36
- Firebase Authentication (anonymous starter identity)
- Cloud Firestore complaint records + tracking IDs
- Complaint status tracking
- GPS capture with permission
- 24/7 helpline 0343 9398790
- Firebase Cloud Messaging service hook
- Firebase App Check dependency
- Firestore + Storage security rules
- Browser Admin Panel files
- Cloud Function status-change hook

## Required before public launch
The department must own a Firebase project and a Google Play Console account. Add `app/google-services.json` from that Firebase project, enable Authentication/Firestore/Storage/App Check, create authorized admin users, and deploy the supplied rules/functions. Replace the Firebase web config in the admin panel and host it. Then build a signed AAB and publish through Play Console.

Do not represent the app as an official government app until the competent authority has approved the branding, data collection, privacy policy, and publishing account.
