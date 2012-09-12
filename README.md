Android Notifier
------

# Overview

Notifier is a non-Play-Store Android app free for personal use.
The app is a simple target for Android's Google Cloud Messaging service (essentially, push notifications). The intent is to provide a notification point for any arbitrary script.
Cron jobs, server monitoring, anything that can send an HTTP request (or execute a command to something else that can).
It is currently not on the Play Store because it is currently not fit for the general public: it would need a proper server-side API to mask my GCM account,
or a phone-side setting to allow it to be changed to the hypothetical user's GCM account.

If you want to try it out, the authorization key paired with the app is: AIzaSyAXr3JxOay1bTaEMNVxguaGDcR3Vm2i190 . However, if you actually want to **use** it, please sign up for
your own GCM account and change the constants `PATH` and `APP_GCM_ID` in `src/rickflail.messaging.notifier/Registration.java`.

## Installation

1. On your Android device, go to Settings -> Security.
2. Check "Unknown sources".
3. Get the .apk file (located in the root) onto your device however you choose.
4. Execute the .apk

## Usage

The app will do nothing without a third-party server to send messages to it. See http://developer.android.com/guide/google/gcm/gcm.html#server for details.

The server must respond to a registration request and store the device's ID in order to be able to send to it in the future.
It will receive a `POST` request with a constant string `auth`, a `name` (the user's Google account before the @), and an `id` (the part that will be used to send).

Then, to send a message, the server must `POST` as detailed here: http://developer.android.com/guide/google/gcm/gcm.html#request .
The data parameter in GCM can be anything, but Notifier accepts data as an object with the following parameters:

- title (will be shown in the notifications bar)
- message (optional smaller text underneath)
- key (optional: if a message is received with the same key as an older message, the new one will overwrite it instead of adding)
- silent (optional: this will add / update the message in the Notifier interface, but not send an Android notification)