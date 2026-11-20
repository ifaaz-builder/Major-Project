package com.scamshield.ai.calls

/** Fixed, privacy-safe speech. No caller or message content is accepted here. */
object SpokenPhraseCatalog {
    const val CALL_SCAM = "call_scam"
    const val CALL_SPAM = "call_spam"
    const val MESSAGE_SECRET = "message_secret"
    const val MESSAGE_ACCOUNT = "message_account"
    const val MESSAGE_LINK = "message_link"
    const val MESSAGE_GENERIC = "message_generic"
    const val MANUAL_DANGER = "manual_danger"
    const val MANUAL_SAFE = "manual_safe"
    const val LINK_DANGER = "link_danger"
    const val LINK_CAUTION = "link_caution"
    const val LINK_SAFE = "link_safe"
    const val TEST = "speech_test"

    private val phrases: Map<String, Map<String, String>> = mapOf(
        "en" to mapOf(
            CALL_SCAM to "Warning. This caller may be a scammer. Be careful.",
            CALL_SPAM to "Warning. This caller is reported as spam. Do not share personal information.",
            MESSAGE_SECRET to "Warning. This message asks for a secret code or password. Never share an OTP, PIN, or password.",
            MESSAGE_ACCOUNT to "Warning. This message creates urgency about an account or payment. Contact the bank using a number you already trust.",
            MESSAGE_LINK to "Warning. This message contains a suspicious link. Do not open it or enter personal details.",
            MESSAGE_GENERIC to "Warning. This message may be a scam. Do not share codes or passwords, send money, or open suspicious links.",
            MANUAL_DANGER to "Warning. This message may be a scam. Never share an OTP, PIN, or password, and do not open suspicious links.",
            MANUAL_SAFE to "No common scam pattern was found. Still, never share an OTP, PIN, or password.",
            LINK_DANGER to "Warning. This link or file looks dangerous. Do not open it or install it. Verify with the company using a trusted number.",
            LINK_CAUTION to "Be careful. This link or file looks suspicious. Do not share a code or password. Verify it first.",
            LINK_SAFE to "No common scam pattern was found in this link or file. Only open items you were expecting.",
            TEST to "ScamShield speech test. This is only a test. No warning was generated."
        ),
        "hi" to mapOf(
            CALL_SCAM to "सावधान। यह कॉल धोखाधड़ी करने वाले की हो सकती है। कृपया सतर्क रहें।",
            CALL_SPAM to "सावधान। इस कॉल को स्पैम बताया गया है। अपनी निजी जानकारी साझा न करें।",
            MESSAGE_SECRET to "सावधान। इस संदेश में गुप्त कोड या पासवर्ड माँगा गया है। ओटीपी, पिन या पासवर्ड कभी साझा न करें।",
            MESSAGE_ACCOUNT to "सावधान। यह संदेश खाते या भुगतान को लेकर जल्दी करने का दबाव डालता है। बैंक को केवल पहले से भरोसेमंद नंबर पर कॉल करें।",
            MESSAGE_LINK to "सावधान। इस संदेश में संदिग्ध लिंक है। इसे न खोलें और निजी जानकारी न भरें।",
            MESSAGE_GENERIC to "सावधान। यह संदेश धोखाधड़ी हो सकता है। कोड या पासवर्ड साझा न करें, पैसे न भेजें और संदिग्ध लिंक न खोलें।",
            MANUAL_DANGER to "सावधान। यह संदेश धोखाधड़ी हो सकता है। ओटीपी, पिन या पासवर्ड साझा न करें और संदिग्ध लिंक न खोलें।",
            MANUAL_SAFE to "धोखाधड़ी का कोई आम संकेत नहीं मिला। फिर भी ओटीपी, पिन या पासवर्ड कभी साझा न करें।",
            LINK_DANGER to "सावधान। यह लिंक या फ़ाइल खतरनाक लगती है। इसे न खोलें और इंस्टॉल न करें। कंपनी से भरोसेमंद नंबर पर पुष्टि करें।",
            LINK_CAUTION to "सतर्क रहें। यह लिंक या फ़ाइल संदिग्ध लगती है। कोई कोड या पासवर्ड साझा न करें। पहले पुष्टि करें।",
            LINK_SAFE to "इस लिंक या फ़ाइल में धोखाधड़ी का आम संकेत नहीं मिला। केवल वही चीज़ खोलें जिसकी आप अपेक्षा कर रहे थे।",
            TEST to "स्कैमशील्ड का आवाज़ परीक्षण। यह केवल एक परीक्षण है। कोई चेतावनी नहीं बनाई गई है।"
        ),
        "ml" to mapOf(
            CALL_SCAM to "ശ്രദ്ധിക്കുക. ഈ വിളി തട്ടിപ്പുകാരനിൽ നിന്നാകാം. ജാഗ്രത പാലിക്കുക.",
            CALL_SPAM to "ശ്രദ്ധിക്കുക. ഈ വിളി സ്പാം ആയി റിപ്പോർട്ട് ചെയ്തിട്ടുണ്ട്. സ്വകാര്യ വിവരങ്ങൾ പങ്കിടരുത്.",
            MESSAGE_SECRET to "ശ്രദ്ധിക്കുക. ഈ സന്ദേശം രഹസ്യ കോഡോ പാസ്‌വേഡോ ചോദിക്കുന്നു. ഒ.ടി.പി., പിൻ, പാസ്‌വേഡ് എന്നിവ ആരോടും പറയരുത്.",
            MESSAGE_ACCOUNT to "ശ്രദ്ധിക്കുക. അക്കൗണ്ടിനെയോ പണമടയ്ക്കലിനെയോ കുറിച്ച് ഈ സന്ദേശം അടിയന്തരത സൃഷ്ടിക്കുന്നു. നിങ്ങൾക്ക് അറിയാവുന്ന നമ്പറിൽ ബാങ്കിനെ വിളിക്കുക.",
            MESSAGE_LINK to "ശ്രദ്ധിക്കുക. ഈ സന്ദേശത്തിൽ സംശയകരമായ ലിങ്കുണ്ട്. അത് തുറക്കരുത്; സ്വകാര്യ വിവരങ്ങൾ നൽകരുത്.",
            MESSAGE_GENERIC to "ശ്രദ്ധിക്കുക. ഈ സന്ദേശം തട്ടിപ്പാകാം. കോഡോ പാസ്‌വേഡോ പറയരുത്, പണം അയക്കരുത്, സംശയകരമായ ലിങ്ക് തുറക്കരുത്.",
            MANUAL_DANGER to "ശ്രദ്ധിക്കുക. ഈ സന്ദേശം തട്ടിപ്പാകാം. ഒ.ടി.പി., പിൻ, പാസ്‌വേഡ് എന്നിവ പറയരുത്; സംശയകരമായ ലിങ്കുകൾ തുറക്കരുത്.",
            MANUAL_SAFE to "സാധാരണ തട്ടിപ്പ് സൂചനകളൊന്നും കണ്ടില്ല. എങ്കിലും ഒ.ടി.പി., പിൻ, പാസ്‌വേഡ് എന്നിവ ആരോടും പറയരുത്.",
            LINK_DANGER to "ശ്രദ്ധിക്കുക. ഈ ലിങ്കോ ഫയലോ അപകടകരമായി തോന്നുന്നു. തുറക്കുകയോ ഇൻസ്റ്റാൾ ചെയ്യുകയോ അരുത്. വിശ്വസനീയ നമ്പറിൽ സ്ഥിരീകരിക്കുക.",
            LINK_CAUTION to "ജാഗ്രത പാലിക്കുക. ഈ ലിങ്കോ ഫയലോ സംശയകരമാണ്. കോഡോ പാസ്‌വേഡോ പങ്കിടരുത്. ആദ്യം സ്ഥിരീകരിക്കുക.",
            LINK_SAFE to "ഈ ലിങ്കിലോ ഫയലിലോ സാധാരണ തട്ടിപ്പ് സൂചന കണ്ടില്ല. നിങ്ങൾ പ്രതീക്ഷിച്ചവ മാത്രം തുറക്കുക.",
            TEST to "സ്കാംഷീൽഡ് ശബ്ദ പരിശോധന. ഇത് ഒരു പരിശോധന മാത്രമാണ്. മുന്നറിയിപ്പ് സൃഷ്ടിച്ചിട്ടില്ല."
        ),
        "kn" to mapOf(
            CALL_SCAM to "ಎಚ್ಚರಿಕೆ. ಈ ಕರೆ ವಂಚಕರದ್ದಾಗಿರಬಹುದು. ದಯವಿಟ್ಟು ಜಾಗರೂಕರಾಗಿರಿ.",
            CALL_SPAM to "ಎಚ್ಚರಿಕೆ. ಈ ಕರೆ ಸ್ಪ್ಯಾಮ್ ಎಂದು ವರದಿಯಾಗಿದೆ. ವೈಯಕ್ತಿಕ ಮಾಹಿತಿಯನ್ನು ಹಂಚಿಕೊಳ್ಳಬೇಡಿ.",
            MESSAGE_SECRET to "ಎಚ್ಚರಿಕೆ. ಈ ಸಂದೇಶ ರಹಸ್ಯ ಕೋಡ್ ಅಥವಾ ಪಾಸ್‌ವರ್ಡ್ ಕೇಳುತ್ತದೆ. ಒಟಿಪಿ, ಪಿನ್ ಅಥವಾ ಪಾಸ್‌ವರ್ಡ್ ಅನ್ನು ಯಾರಿಗೂ ಹೇಳಬೇಡಿ.",
            MESSAGE_ACCOUNT to "ಎಚ್ಚರಿಕೆ. ಈ ಸಂದೇಶ ಖಾತೆ ಅಥವಾ ಪಾವತಿ ಬಗ್ಗೆ ತುರ್ತು ಒತ್ತಡ ಉಂಟುಮಾಡುತ್ತದೆ. ನಿಮಗೆ ತಿಳಿದಿರುವ ಸಂಖ್ಯೆಗೆ ಬ್ಯಾಂಕ್‌ಗೆ ಕರೆ ಮಾಡಿ.",
            MESSAGE_LINK to "ಎಚ್ಚರಿಕೆ. ಈ ಸಂದೇಶದಲ್ಲಿ ಅನುಮಾನಾಸ್ಪದ ಲಿಂಕ್ ಇದೆ. ಅದನ್ನು ತೆರೆಯಬೇಡಿ ಅಥವಾ ವೈಯಕ್ತಿಕ ವಿವರಗಳನ್ನು ನಮೂದಿಸಬೇಡಿ.",
            MESSAGE_GENERIC to "ಎಚ್ಚರಿಕೆ. ಈ ಸಂದೇಶ ವಂಚನೆಯಾಗಿರಬಹುದು. ಕೋಡ್ ಅಥವಾ ಪಾಸ್‌ವರ್ಡ್ ಹಂಚಿಕೊಳ್ಳಬೇಡಿ, ಹಣ ಕಳುಹಿಸಬೇಡಿ ಮತ್ತು ಅನುಮಾನಾಸ್ಪದ ಲಿಂಕ್ ತೆರೆಯಬೇಡಿ.",
            MANUAL_DANGER to "ಎಚ್ಚರಿಕೆ. ಈ ಸಂದೇಶ ವಂಚನೆಯಾಗಿರಬಹುದು. ಒಟಿಪಿ, ಪಿನ್ ಅಥವಾ ಪಾಸ್‌ವರ್ಡ್ ಹಂಚಿಕೊಳ್ಳಬೇಡಿ; ಅನುಮಾನಾಸ್ಪದ ಲಿಂಕ್ ತೆರೆಯಬೇಡಿ.",
            MANUAL_SAFE to "ಸಾಮಾನ್ಯ ವಂಚನೆ ಸೂಚನೆ ಕಂಡುಬಂದಿಲ್ಲ. ಆದರೂ ಒಟಿಪಿ, ಪಿನ್ ಅಥವಾ ಪಾಸ್‌ವರ್ಡ್ ಯಾರಿಗೂ ಹೇಳಬೇಡಿ.",
            LINK_DANGER to "ಎಚ್ಚರಿಕೆ. ಈ ಲಿಂಕ್ ಅಥವಾ ಫೈಲ್ ಅಪಾಯಕಾರಿಯಂತೆ ಕಾಣುತ್ತದೆ. ತೆರೆಯಬೇಡಿ ಅಥವಾ ಇನ್‌ಸ್ಟಾಲ್ ಮಾಡಬೇಡಿ. ತಿಳಿದಿರುವ ಸಂಖ್ಯೆಯಿಂದ ದೃಢಪಡಿಸಿ.",
            LINK_CAUTION to "ಜಾಗರೂಕರಾಗಿರಿ. ಈ ಲಿಂಕ್ ಅಥವಾ ಫೈಲ್ ಅನುಮಾನಾಸ್ಪದವಾಗಿದೆ. ಕೋಡ್ ಅಥವಾ ಪಾಸ್‌ವರ್ಡ್ ಹಂಚಿಕೊಳ್ಳಬೇಡಿ. ಮೊದಲು ದೃಢಪಡಿಸಿ.",
            LINK_SAFE to "ಈ ಲಿಂಕ್ ಅಥವಾ ಫೈಲ್‌ನಲ್ಲಿ ಸಾಮಾನ್ಯ ವಂಚನೆ ಸೂಚನೆ ಕಂಡಿಲ್ಲ. ನೀವು ನಿರೀಕ್ಷಿಸಿದದ್ದನ್ನೇ ತೆರೆಯಿರಿ.",
            TEST to "ಸ್ಕ್ಯಾಮ್‌ಶೀಲ್ಡ್ ಧ್ವನಿ ಪರೀಕ್ಷೆ. ಇದು ಪರೀಕ್ಷೆ ಮಾತ್ರ. ಯಾವುದೇ ಎಚ್ಚರಿಕೆ ಸೃಷ್ಟಿಯಾಗಿಲ್ಲ."
        ),
        "ta" to mapOf(
            CALL_SCAM to "எச்சரிக்கை. இந்த அழைப்பு மோசடி செய்பவரிடமிருந்து வந்திருக்கலாம். கவனமாக இருங்கள்.",
            CALL_SPAM to "எச்சரிக்கை. இந்த அழைப்பு ஸ்பாம் எனப் புகாரளிக்கப்பட்டுள்ளது. தனிப்பட்ட தகவல்களைப் பகிர வேண்டாம்.",
            MESSAGE_SECRET to "எச்சரிக்கை. இந்தச் செய்தி ரகசியக் குறியீடு அல்லது கடவுச்சொல்லைக் கேட்கிறது. OTP, PIN அல்லது கடவுச்சொல்லை யாரிடமும் பகிர வேண்டாம்.",
            MESSAGE_ACCOUNT to "எச்சரிக்கை. கணக்கு அல்லது பணம் குறித்து இந்தச் செய்தி அவசரப்படுத்துகிறது. உங்களுக்குத் தெரிந்த எண்ணில் வங்கியைத் தொடர்பு கொள்ளுங்கள்.",
            MESSAGE_LINK to "எச்சரிக்கை. இந்தச் செய்தியில் சந்தேகமான இணைப்பு உள்ளது. அதைத் திறக்கவோ தனிப்பட்ட விவரங்களை உள்ளிடவோ வேண்டாம்.",
            MESSAGE_GENERIC to "எச்சரிக்கை. இந்தச் செய்தி மோசடியாக இருக்கலாம். குறியீடு அல்லது கடவுச்சொல்லைப் பகிர வேண்டாம்; பணம் அனுப்பவோ சந்தேகமான இணைப்பைத் திறக்கவோ வேண்டாம்.",
            MANUAL_DANGER to "எச்சரிக்கை. இந்தச் செய்தி மோசடியாக இருக்கலாம். OTP, PIN அல்லது கடவுச்சொல்லைப் பகிர வேண்டாம்; சந்தேகமான இணைப்பைத் திறக்க வேண்டாம்.",
            MANUAL_SAFE to "பொதுவான மோசடி அறிகுறி காணப்படவில்லை. இருப்பினும் OTP, PIN அல்லது கடவுச்சொல்லை யாரிடமும் பகிர வேண்டாம்.",
            LINK_DANGER to "எச்சரிக்கை. இந்த இணைப்பு அல்லது கோப்பு ஆபத்தாகத் தெரிகிறது. திறக்கவோ நிறுவவோ வேண்டாம். நம்பகமான எண்ணில் உறுதிப்படுத்துங்கள்.",
            LINK_CAUTION to "கவனமாக இருங்கள். இந்த இணைப்பு அல்லது கோப்பு சந்தேகமாக உள்ளது. குறியீடு அல்லது கடவுச்சொல்லைப் பகிர வேண்டாம். முதலில் உறுதிப்படுத்துங்கள்.",
            LINK_SAFE to "இந்த இணைப்பு அல்லது கோப்பில் பொதுவான மோசடி அறிகுறி காணப்படவில்லை. நீங்கள் எதிர்பார்த்தவற்றை மட்டும் திறக்கவும்.",
            TEST to "ஸ்கேம்ஷீல்டின் குரல் சோதனை. இது ஒரு சோதனை மட்டுமே. எச்சரிக்கை உருவாக்கப்படவில்லை."
        ),
        "te" to mapOf(
            CALL_SCAM to "హెచ్చరిక. ఈ కాల్ మోసగాడి నుంచి వచ్చి ఉండవచ్చు. జాగ్రత్తగా ఉండండి.",
            CALL_SPAM to "హెచ్చరిక. ఈ కాల్ స్పామ్‌గా నివేదించబడింది. వ్యక్తిగత సమాచారాన్ని పంచుకోవద్దు.",
            MESSAGE_SECRET to "హెచ్చరిక. ఈ సందేశం రహస్య కోడ్ లేదా పాస్‌వర్డ్ అడుగుతోంది. ఓటీపీ, పిన్ లేదా పాస్‌వర్డ్ ఎవరికీ చెప్పవద్దు.",
            MESSAGE_ACCOUNT to "హెచ్చరిక. ఖాతా లేదా చెల్లింపు గురించి ఈ సందేశం తొందర పెడుతోంది. మీకు తెలిసిన నంబర్‌కు బ్యాంకుకు కాల్ చేయండి.",
            MESSAGE_LINK to "హెచ్చరిక. ఈ సందేశంలో అనుమానాస్పద లింక్ ఉంది. దాన్ని తెరవవద్దు; వ్యక్తిగత వివరాలు ఇవ్వవద్దు.",
            MESSAGE_GENERIC to "హెచ్చరిక. ఈ సందేశం మోసం కావచ్చు. కోడ్ లేదా పాస్‌వర్డ్ పంచుకోవద్దు, డబ్బు పంపవద్దు, అనుమానాస్పద లింక్ తెరవవద్దు.",
            MANUAL_DANGER to "హెచ్చరిక. ఈ సందేశం మోసం కావచ్చు. ఓటీపీ, పిన్ లేదా పాస్‌వర్డ్ పంచుకోవద్దు; అనుమానాస్పద లింక్ తెరవవద్దు.",
            MANUAL_SAFE to "సాధారణ మోసం సంకేతాలు కనిపించలేదు. అయినా ఓటీపీ, పిన్ లేదా పాస్‌వర్డ్ ఎవరికీ చెప్పవద్దు.",
            LINK_DANGER to "హెచ్చరిక. ఈ లింక్ లేదా ఫైల్ ప్రమాదకరంగా ఉంది. తెరవవద్దు లేదా ఇన్‌స్టాల్ చేయవద్దు. నమ్మకమైన నంబర్‌తో నిర్ధారించండి.",
            LINK_CAUTION to "జాగ్రత్తగా ఉండండి. ఈ లింక్ లేదా ఫైల్ అనుమానాస్పదంగా ఉంది. కోడ్ లేదా పాస్‌వర్డ్ పంచుకోవద్దు. ముందుగా నిర్ధారించండి.",
            LINK_SAFE to "ఈ లింక్ లేదా ఫైల్‌లో సాధారణ మోసం సంకేతాలు కనిపించలేదు. మీరు ఆశించిన వాటినే తెరవండి.",
            TEST to "స్కామ్‌షీల్డ్ వాయిస్ పరీక్ష. ఇది పరీక్ష మాత్రమే. ఎలాంటి హెచ్చరిక సృష్టించబడలేదు."
        ),
        "bn" to mapOf(
            CALL_SCAM to "সতর্কতা। এই কলটি প্রতারকের হতে পারে। সাবধানে থাকুন।",
            CALL_SPAM to "সতর্কতা। এই কলটি স্প্যাম হিসেবে জানানো হয়েছে। ব্যক্তিগত তথ্য দেবেন না।",
            MESSAGE_SECRET to "সতর্কতা। এই বার্তায় গোপন কোড বা পাসওয়ার্ড চাওয়া হয়েছে। ওটিপি, পিন বা পাসওয়ার্ড কাউকে বলবেন না।",
            MESSAGE_ACCOUNT to "সতর্কতা। অ্যাকাউন্ট বা টাকা নিয়ে এই বার্তাটি তাড়া দিচ্ছে। আগে থেকে জানা নম্বরে ব্যাংকে ফোন করুন।",
            MESSAGE_LINK to "সতর্কতা। এই বার্তায় সন্দেহজনক লিঙ্ক আছে। এটি খুলবেন না বা ব্যক্তিগত তথ্য দেবেন না।",
            MESSAGE_GENERIC to "সতর্কতা। এই বার্তাটি প্রতারণা হতে পারে। কোড বা পাসওয়ার্ড দেবেন না, টাকা পাঠাবেন না, সন্দেহজনক লিঙ্ক খুলবেন না।",
            MANUAL_DANGER to "সতর্কতা। এই বার্তাটি প্রতারণা হতে পারে। ওটিপি, পিন বা পাসওয়ার্ড দেবেন না এবং সন্দেহজনক লিঙ্ক খুলবেন না।",
            MANUAL_SAFE to "সাধারণ প্রতারণার লক্ষণ পাওয়া যায়নি। তবুও ওটিপি, পিন বা পাসওয়ার্ড কাউকে বলবেন না।",
            LINK_DANGER to "সতর্কতা। এই লিঙ্ক বা ফাইল বিপজ্জনক মনে হচ্ছে। খুলবেন না বা ইনস্টল করবেন না। পরিচিত নম্বরে যাচাই করুন।",
            LINK_CAUTION to "সাবধান থাকুন। এই লিঙ্ক বা ফাইল সন্দেহজনক। কোড বা পাসওয়ার্ড দেবেন না। আগে যাচাই করুন।",
            LINK_SAFE to "এই লিঙ্ক বা ফাইলে সাধারণ প্রতারণার লক্ষণ পাওয়া যায়নি। শুধু প্রত্যাশিত জিনিস খুলুন।",
            TEST to "স্ক্যামশিল্ডের কণ্ঠ পরীক্ষা। এটি শুধু একটি পরীক্ষা। কোনো সতর্কতা তৈরি হয়নি।"
        ),
        "mr" to mapOf(
            CALL_SCAM to "सावधान. हा कॉल फसवणूक करणाऱ्याचा असू शकतो. काळजी घ्या.",
            CALL_SPAM to "सावधान. हा कॉल स्पॅम म्हणून नोंदवला आहे. वैयक्तिक माहिती सांगू नका.",
            MESSAGE_SECRET to "सावधान. या संदेशात गुप्त कोड किंवा पासवर्ड मागितला आहे. ओटीपी, पिन किंवा पासवर्ड कोणालाही सांगू नका.",
            MESSAGE_ACCOUNT to "सावधान. खात्याबद्दल किंवा पैशांबद्दल हा संदेश घाई करायला लावतो. आधीपासून माहीत असलेल्या क्रमांकावर बँकेला फोन करा.",
            MESSAGE_LINK to "सावधान. या संदेशात संशयास्पद लिंक आहे. ती उघडू नका आणि वैयक्तिक माहिती भरू नका.",
            MESSAGE_GENERIC to "सावधान. हा संदेश फसवणूक असू शकतो. कोड किंवा पासवर्ड सांगू नका, पैसे पाठवू नका आणि संशयास्पद लिंक उघडू नका.",
            MANUAL_DANGER to "सावधान. हा संदेश फसवणूक असू शकतो. ओटीपी, पिन किंवा पासवर्ड सांगू नका आणि संशयास्पद लिंक उघडू नका.",
            MANUAL_SAFE to "फसवणुकीची सामान्य चिन्हे आढळली नाहीत. तरीही ओटीपी, पिन किंवा पासवर्ड कोणालाही सांगू नका.",
            LINK_DANGER to "सावधान. ही लिंक किंवा फाइल धोकादायक वाटते. ती उघडू किंवा इन्स्टॉल करू नका. ओळखीच्या क्रमांकावर खात्री करा.",
            LINK_CAUTION to "काळजी घ्या. ही लिंक किंवा फाइल संशयास्पद आहे. कोड किंवा पासवर्ड सांगू नका. आधी खात्री करा.",
            LINK_SAFE to "या लिंक किंवा फाइलमध्ये फसवणुकीची सामान्य चिन्हे आढळली नाहीत. अपेक्षित असलेल्याच गोष्टी उघडा.",
            TEST to "स्कॅमशील्ड आवाज चाचणी. ही फक्त चाचणी आहे. कोणताही इशारा तयार केलेला नाही."
        ),
        "gu" to mapOf(
            CALL_SCAM to "ચેતવણી. આ કૉલ ઠગનો હોઈ શકે છે. સાવચેત રહો.",
            CALL_SPAM to "ચેતવણી. આ કૉલ સ્પૅમ તરીકે નોંધાયો છે. તમારી અંગત માહિતી આપશો નહીં.",
            MESSAGE_SECRET to "ચેતવણી. આ સંદેશ ગુપ્ત કોડ અથવા પાસવર્ડ માંગે છે. ઓટીપી, પિન કે પાસવર્ડ કોઈને આપશો નહીં.",
            MESSAGE_ACCOUNT to "ચેતવણી. ખાતા અથવા ચુકવણી વિશે આ સંદેશ ઉતાવળ કરાવે છે. પહેલેથી જાણતા નંબર પર બેંકને ફોન કરો.",
            MESSAGE_LINK to "ચેતવણી. આ સંદેશમાં શંકાસ્પદ લિંક છે. તેને ખોલશો નહીં કે અંગત વિગતો ભરશો નહીં.",
            MESSAGE_GENERIC to "ચેતવણી. આ સંદેશ છેતરપિંડી હોઈ શકે છે. કોડ કે પાસવર્ડ ન આપો, પૈસા ન મોકલો અને શંકાસ્પદ લિંક ન ખોલો.",
            MANUAL_DANGER to "ચેતવણી. આ સંદેશ છેતરપિંડી હોઈ શકે છે. ઓટીપી, પિન કે પાસવર્ડ ન આપો અને શંકાસ્પદ લિંક ન ખોલો.",
            MANUAL_SAFE to "છેતરપિંડીનાં સામાન્ય સંકેત મળ્યા નથી. તેમ છતાં ઓટીપી, પિન કે પાસવર્ડ કોઈને આપશો નહીં.",
            LINK_DANGER to "ચેતવણી. આ લિંક અથવા ફાઇલ જોખમી લાગે છે. તેને ખોલશો કે ઇન્સ્ટોલ કરશો નહીં. જાણીતા નંબર પર ખાતરી કરો.",
            LINK_CAUTION to "સાવચેત રહો. આ લિંક અથવા ફાઇલ શંકાસ્પદ છે. કોડ કે પાસવર્ડ ન આપો. પહેલાં ખાતરી કરો.",
            LINK_SAFE to "આ લિંક અથવા ફાઇલમાં છેતરપિંડીનો સામાન્ય સંકેત મળ્યો નથી. તમે જેની અપેક્ષા રાખતા હો તે જ ખોલો.",
            TEST to "સ્કૅમશીલ્ડ અવાજ પરીક્ષણ. આ માત્ર પરીક્ષણ છે. કોઈ ચેતવણી બનાવાઈ નથી."
        ),
        "pa" to mapOf(
            CALL_SCAM to "ਸਾਵਧਾਨ। ਇਹ ਕਾਲ ਧੋਖੇਬਾਜ਼ ਦੀ ਹੋ ਸਕਦੀ ਹੈ। ਧਿਆਨ ਰੱਖੋ।",
            CALL_SPAM to "ਸਾਵਧਾਨ। ਇਹ ਕਾਲ ਸਪੈਮ ਵਜੋਂ ਰਿਪੋਰਟ ਹੋਈ ਹੈ। ਨਿੱਜੀ ਜਾਣਕਾਰੀ ਨਾ ਦਿਓ।",
            MESSAGE_SECRET to "ਸਾਵਧਾਨ। ਇਸ ਸੁਨੇਹੇ ਵਿੱਚ ਗੁਪਤ ਕੋਡ ਜਾਂ ਪਾਸਵਰਡ ਮੰਗਿਆ ਗਿਆ ਹੈ। ਓਟੀਪੀ, ਪਿੰਨ ਜਾਂ ਪਾਸਵਰਡ ਕਿਸੇ ਨੂੰ ਨਾ ਦੱਸੋ।",
            MESSAGE_ACCOUNT to "ਸਾਵਧਾਨ। ਖਾਤੇ ਜਾਂ ਭੁਗਤਾਨ ਬਾਰੇ ਇਹ ਸੁਨੇਹਾ ਜਲਦੀ ਕਰਨ ਲਈ ਦਬਾਅ ਪਾਉਂਦਾ ਹੈ। ਬੈਂਕ ਨੂੰ ਜਾਣੇ-ਪਛਾਣੇ ਨੰਬਰ ਤੇ ਕਾਲ ਕਰੋ।",
            MESSAGE_LINK to "ਸਾਵਧਾਨ। ਇਸ ਸੁਨੇਹੇ ਵਿੱਚ ਸ਼ੱਕੀ ਲਿੰਕ ਹੈ। ਇਸਨੂੰ ਨਾ ਖੋਲ੍ਹੋ ਅਤੇ ਨਿੱਜੀ ਜਾਣਕਾਰੀ ਨਾ ਭਰੋ।",
            MESSAGE_GENERIC to "ਸਾਵਧਾਨ। ਇਹ ਸੁਨੇਹਾ ਧੋਖਾ ਹੋ ਸਕਦਾ ਹੈ। ਕੋਡ ਜਾਂ ਪਾਸਵਰਡ ਨਾ ਦਿਓ, ਪੈਸੇ ਨਾ ਭੇਜੋ ਅਤੇ ਸ਼ੱਕੀ ਲਿੰਕ ਨਾ ਖੋਲ੍ਹੋ।",
            MANUAL_DANGER to "ਸਾਵਧਾਨ। ਇਹ ਸੁਨੇਹਾ ਧੋਖਾ ਹੋ ਸਕਦਾ ਹੈ। ਓਟੀਪੀ, ਪਿੰਨ ਜਾਂ ਪਾਸਵਰਡ ਨਾ ਦਿਓ ਅਤੇ ਸ਼ੱਕੀ ਲਿੰਕ ਨਾ ਖੋਲ੍ਹੋ।",
            MANUAL_SAFE to "ਧੋਖਾਧੜੀ ਦੇ ਆਮ ਸੰਕੇਤ ਨਹੀਂ ਮਿਲੇ। ਫਿਰ ਵੀ ਓਟੀਪੀ, ਪਿੰਨ ਜਾਂ ਪਾਸਵਰਡ ਕਿਸੇ ਨੂੰ ਨਾ ਦੱਸੋ।",
            LINK_DANGER to "ਸਾਵਧਾਨ। ਇਹ ਲਿੰਕ ਜਾਂ ਫ਼ਾਈਲ ਖ਼ਤਰਨਾਕ ਲੱਗਦੀ ਹੈ। ਇਸਨੂੰ ਨਾ ਖੋਲ੍ਹੋ ਜਾਂ ਇੰਸਟਾਲ ਨਾ ਕਰੋ। ਜਾਣੇ-ਪਛਾਣੇ ਨੰਬਰ ਤੋਂ ਪੱਕਾ ਕਰੋ।",
            LINK_CAUTION to "ਧਿਆਨ ਰੱਖੋ। ਇਹ ਲਿੰਕ ਜਾਂ ਫ਼ਾਈਲ ਸ਼ੱਕੀ ਹੈ। ਕੋਡ ਜਾਂ ਪਾਸਵਰਡ ਨਾ ਦਿਓ। ਪਹਿਲਾਂ ਪੱਕਾ ਕਰੋ।",
            LINK_SAFE to "ਇਸ ਲਿੰਕ ਜਾਂ ਫ਼ਾਈਲ ਵਿੱਚ ਧੋਖਾਧੜੀ ਦੇ ਆਮ ਸੰਕੇਤ ਨਹੀਂ ਮਿਲੇ। ਸਿਰਫ਼ ਉਹੀ ਖੋਲ੍ਹੋ ਜਿਸਦੀ ਤੁਸੀਂ ਉਮੀਦ ਕਰਦੇ ਹੋ।",
            TEST to "ਸਕੈਮਸ਼ੀਲਡ ਆਵਾਜ਼ ਜਾਂਚ। ਇਹ ਸਿਰਫ਼ ਜਾਂਚ ਹੈ। ਕੋਈ ਚੇਤਾਵਨੀ ਨਹੀਂ ਬਣਾਈ ਗਈ।"
        ),
        "ur" to mapOf(
            CALL_SCAM to "خبردار۔ یہ کال کسی دھوکے باز کی ہو سکتی ہے۔ احتیاط کریں۔",
            CALL_SPAM to "خبردار۔ اس کال کی اطلاع اسپام کے طور پر دی گئی ہے۔ ذاتی معلومات نہ دیں۔",
            MESSAGE_SECRET to "خبردار۔ اس پیغام میں خفیہ کوڈ یا پاس ورڈ مانگا گیا ہے۔ او ٹی پی، پن یا پاس ورڈ کسی کو نہ بتائیں۔",
            MESSAGE_ACCOUNT to "خبردار۔ یہ پیغام اکاؤنٹ یا ادائیگی کے بارے میں جلدی کرنے کا دباؤ ڈالتا ہے۔ بینک کو پہلے سے معلوم نمبر پر فون کریں۔",
            MESSAGE_LINK to "خبردار۔ اس پیغام میں مشکوک لنک ہے۔ اسے نہ کھولیں اور ذاتی تفصیل نہ دیں۔",
            MESSAGE_GENERIC to "خبردار۔ یہ پیغام دھوکا ہو سکتا ہے۔ کوڈ یا پاس ورڈ نہ دیں، پیسے نہ بھیجیں اور مشکوک لنک نہ کھولیں۔",
            MANUAL_DANGER to "خبردار۔ یہ پیغام دھوکا ہو سکتا ہے۔ او ٹی پی، پن یا پاس ورڈ نہ دیں اور مشکوک لنک نہ کھولیں۔",
            MANUAL_SAFE to "دھوکے کی عام علامت نہیں ملی۔ پھر بھی او ٹی پی، پن یا پاس ورڈ کسی کو نہ بتائیں۔",
            LINK_DANGER to "خبردار۔ یہ لنک یا فائل خطرناک لگتی ہے۔ اسے نہ کھولیں اور نہ انسٹال کریں۔ معلوم نمبر پر تصدیق کریں۔",
            LINK_CAUTION to "احتیاط کریں۔ یہ لنک یا فائل مشکوک ہے۔ کوڈ یا پاس ورڈ نہ دیں۔ پہلے تصدیق کریں۔",
            LINK_SAFE to "اس لنک یا فائل میں دھوکے کی عام علامت نہیں ملی۔ صرف وہی چیز کھولیں جس کی آپ توقع کر رہے تھے۔",
            TEST to "اسکام شیلڈ کی آواز کا امتحان۔ یہ صرف ایک امتحان ہے۔ کوئی انتباہ نہیں بنایا گیا۔"
        ),
        "es" to mapOf(
            CALL_SCAM to "Aviso. Esta llamada podría ser de un estafador. Tenga cuidado.",
            CALL_SPAM to "Aviso. Esta llamada se ha denunciado como spam. No comparta información personal.",
            MESSAGE_SECRET to "Aviso. Este mensaje pide un código secreto o una contraseña. Nunca comparta su código, PIN ni contraseña.",
            MESSAGE_ACCOUNT to "Aviso. Este mensaje le mete prisa sobre una cuenta o un pago. Llame al banco a un número que ya conozca.",
            MESSAGE_LINK to "Aviso. Este mensaje contiene un enlace sospechoso. No lo abra ni introduzca datos personales.",
            MESSAGE_GENERIC to "Aviso. Este mensaje podría ser una estafa. No comparta códigos ni contraseñas, no envíe dinero y no abra enlaces sospechosos.",
            MANUAL_DANGER to "Aviso. Este mensaje podría ser una estafa. No comparta códigos, PIN ni contraseñas y no abra enlaces sospechosos.",
            MANUAL_SAFE to "No se encontró una señal común de estafa. Aun así, nunca comparta códigos, PIN ni contraseñas.",
            LINK_DANGER to "Aviso. Este enlace o archivo parece peligroso. No lo abra ni lo instale. Confirme con la empresa usando un número de confianza.",
            LINK_CAUTION to "Tenga cuidado. Este enlace o archivo parece sospechoso. No comparta códigos ni contraseñas. Compruébelo primero.",
            LINK_SAFE to "No se encontró una señal común de estafa en este enlace o archivo. Abra solo lo que esperaba recibir.",
            TEST to "Prueba de voz de ScamShield. Esto es solo una prueba. No se ha generado ninguna alerta."
        )
    )

    fun phrase(language: String?, phraseId: String): String? =
        phrases[language]?.get(phraseId)

    fun supportedLanguages(): Set<String> = phrases.keys

    fun messagePhrase(matchedRules: Collection<String>): String = when {
        "secret_request" in matchedRules -> MESSAGE_SECRET
        "urgent_payment_or_account_action" in matchedRules || "account_threat_with_secret_or_link" in matchedRules -> MESSAGE_ACCOUNT
        "link_with_account_or_verification_language" in matchedRules || "shortened_link" in matchedRules ||
            "numeric_ip_link" in matchedRules || "internationalized_domain" in matchedRules || "brand_domain_mismatch" in matchedRules -> MESSAGE_LINK
        else -> MESSAGE_GENERIC
    }
}
