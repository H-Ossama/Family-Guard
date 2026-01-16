# Deploying Family Guard Cloud Relay to Render

Follow these steps to move your app from local network sync to cloud-based remote control.

## 1. Prepare for Deployment
1. Create a new repository on GitHub (e.g., `family-guard-relay`).
2. Copy the contents of the `cloud-relay-server/` folder into this new repository.
3. Commit and push the code to GitHub.

## 2. Deploy to Render
1. Go to [Render.com](https://render.com/) and create a free account.
2. Click **New +** and select **Web Service**.
3. Connect your GitHub repository.
4. Configure the service:
   - **Name**: `family-guard-relay` (or your choice)
   - **Environment**: `Node`
   - **Region**: Chooser one closest to you
   - **Plan**: `Free`
   - **Build Command**: `npm install`
   - **Start Command**: `node index.js`
5. Click **Advanced** and add an Environment Variable:
   - **Key**: `JWT_SECRET`
   - **Value**: A long random string (e.g., `8d2f1b7a9c4e2f3g1h5j8k9l0m7n6b5v`)
6. Click **Create Web Service**.

## 3. Prevent Free Tier Sleeping (CRITICAL)
Render's free tier sleeps after 15 minutes of inactivity. To keep your lock/unlock commands fast, you MUST set up a keep-alive ping.

1. Go to [cron-job.org](https://cron-job.org/) (it's free).
2. Create an account and click **Create Cronjob**.
3. Set the following:
   - **Title**: `Family Guard Keep-Alive`
   - **URL**: `https://your-app-name.onrender.com/health`
   - **Execution Schedule**: Every 5 or 10 minutes.
4. Save the cronjob.

## 4. Update Android Apps
Once deployed, copy your Render URL (it will look like `https://abc-xyz.onrender.com`) and update it in:
`common/src/main/java/com/parentalguard/common/network/CloudConfig.kt`

```kotlin
object CloudConfig {
    const val BASE_URL = "https://your-app-name.onrender.com"
    const val WS_URL = "wss://your-app-name.onrender.com"
    // ...
}
```

## 5. Re-pair Devices
After updating the apps with the new Cloud URL:
1. Uninstall the old apps from both devices.
2. Install the new versions.
3. Scan the QR code again to establish the cloud-based link.

Your app will now work over 4G/5G and different WiFi networks!
