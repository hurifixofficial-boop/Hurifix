package com.example.util

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import com.example.data.model.CustomerJobEntity
import com.example.data.model.ExpertEntity
import java.net.URLEncoder

object WhatsAppHelper {

    /**
     * Cleans phone number for WhatsApp URL (adds Indian country code 91 if 10-digit number).
     */
    fun formatPhoneNumberForWhatsApp(phone: String): String {
        val digitsOnly = phone.replace(Regex("[^0-9]"), "")
        return when {
            digitsOnly.length == 10 -> "91$digitsOnly"
            digitsOnly.length == 11 && digitsOnly.startsWith("0") -> "91${digitsOnly.substring(1)}"
            digitsOnly.length == 12 && digitsOnly.startsWith("91") -> digitsOnly
            else -> digitsOnly
        }
    }

    /**
     * Builds the complete dispatch message for the expert with customer details and Google Maps location.
     */
    fun createDispatchMessage(
        expert: ExpertEntity,
        customer: CustomerJobEntity,
        distanceKm: Double,
        travelTimeMinutes: Int
    ): String {
        val mapsUrl = LocationHelper.createGoogleMapsUrl(customer.latitude, customer.longitude)
        val distanceStr = LocationHelper.formatDistance(distanceKm)

        return """
🔧 *HURIFIX DISPATCH ORDER* 🔧
━━━━━━━━━━━━━━━━━━━━
Namaste *${expert.name}* ji, aapke paas ek naya customer task dispatch kiya gaya hai:

👤 *Customer Name:* ${customer.customerName}
📞 *Customer Mobile:* ${customer.customerPhone}
🛠 *Service Required:* ${customer.serviceType}
📝 *Problem / Issue:* ${customer.issueDescription}
📍 *Address:* ${customer.address}

🗺 *Exact Google Maps Location:*
$mapsUrl

📏 *Aapki Duri (Distance):* ~$distanceStr (~$travelTimeMinutes mins)
━━━━━━━━━━━━━━━━━━━━
Kripya turant customer ko call karke time confirm karein.
- *Hurifix Dispatch Team*
        """.trimIndent()
    }

    /**
     * Opens WhatsApp directly with the pre-filled dispatch message for the target expert.
     */
    fun sendWhatsAppMessageToExpert(
        context: Context,
        expert: ExpertEntity,
        customer: CustomerJobEntity,
        distanceKm: Double,
        travelTimeMinutes: Int
    ) {
        val formattedNumber = formatPhoneNumberForWhatsApp(expert.phone)
        val message = createDispatchMessage(expert, customer, distanceKm, travelTimeMinutes)
        val encodedMessage = try {
            URLEncoder.encode(message, "UTF-8")
        } catch (e: Exception) {
            message
        }

        val url = "https://api.whatsapp.com/send?phone=$formattedNumber&text=$encodedMessage"
        val intent = Intent(Intent.ACTION_VIEW).apply {
            data = Uri.parse(url)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            // If WhatsApp is not installed, open via browser or share
            try {
                val fallbackIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(fallbackIntent)
            } catch (e2: Exception) {
                Toast.makeText(context, "WhatsApp nahi mila. Text copy ho gaya hai!", Toast.LENGTH_LONG).show()
                copyToClipboard(context, "Dispatch Message", message)
            }
        }
    }

    /**
     * Launches the phone dialer with the phone number pre-filled.
     */
    fun openDialer(context: Context, phone: String) {
        val cleanPhone = phone.replace(Regex("[^0-9+]"), "")
        val intent = Intent(Intent.ACTION_DIAL).apply {
            data = Uri.parse("tel:$cleanPhone")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "Could not open dialer: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Opens Google Maps navigation to coordinates.
     */
    fun openGoogleMaps(context: Context, latitude: Double, longitude: Double, label: String = "Location") {
        val gmmIntentUri = Uri.parse("geo:$latitude,$longitude?q=$latitude,$longitude($label)")
        val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri).apply {
            setPackage("com.google.android.apps.maps")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        try {
            context.startActivity(mapIntent)
        } catch (e: Exception) {
            // Fallback to browser Google Maps
            val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse(LocationHelper.createGoogleMapsUrl(latitude, longitude))).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(webIntent)
        }
    }

    /**
     * Copies text to system clipboard.
     */
    fun copyToClipboard(context: Context, label: String, text: String) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText(label, text)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(context, "Copied to clipboard!", Toast.LENGTH_SHORT).show()
    }
}
