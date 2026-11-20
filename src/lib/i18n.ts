export type LangCode =
  | "en"
  | "hi"
  | "ml"
  | "kn"
  | "ta"
  | "te"
  | "bn"
  | "mr"
  | "gu"
  | "pa"
  | "ur"
  | "es";

export interface LanguageOption {
  code: LangCode;
  label: string;
  native: string;
  bcp47: string;
}

export const LANGUAGES: LanguageOption[] = [
  { code: "en", label: "English", native: "English", bcp47: "en-US" },
  { code: "hi", label: "Hindi", native: "हिन्दी", bcp47: "hi-IN" },
  { code: "ml", label: "Malayalam", native: "മലയാളം", bcp47: "ml-IN" },
  { code: "kn", label: "Kannada", native: "ಕನ್ನಡ", bcp47: "kn-IN" },
  { code: "ta", label: "Tamil", native: "தமிழ்", bcp47: "ta-IN" },
  { code: "te", label: "Telugu", native: "తెలుగు", bcp47: "te-IN" },
  { code: "bn", label: "Bengali", native: "বাংলা", bcp47: "bn-IN" },
  { code: "mr", label: "Marathi", native: "मराठी", bcp47: "mr-IN" },
  { code: "gu", label: "Gujarati", native: "ગુજરાતી", bcp47: "gu-IN" },
  { code: "pa", label: "Punjabi", native: "ਪੰਜਾਬੀ", bcp47: "pa-IN" },
  { code: "ur", label: "Urdu", native: "اردو", bcp47: "ur-PK" },
  { code: "es", label: "Spanish", native: "Español", bcp47: "es-ES" },
];

export interface Strings {
  dangerHeading: string;
  safeHeading: string;
  dangerTitle: string;
  safeTitle: string;
  cautionTitle: string;
  cautionCoaching: string;
  dangerCoaching: string;
  safeCoaching: string;
  whatThisMeans: string;
  readAloud: string;
  stopReading: string;
  whyFlagged: string;
  languageLabel: string;
}

export const TRANSLATIONS: Record<LangCode, Strings> = {
  en: {
    dangerHeading: "⚠️ DANGER",
    safeHeading: "✅ SAFE",
    dangerTitle: "DANGER: SCAM DETECTED",
    safeTitle: "Safe Message",
    cautionTitle: "Be Careful",
    cautionCoaching: "Do not act yet. Confirm using a number you already trust, and never share a code or password.",
    dangerCoaching:
      "Banks, the government and delivery companies will never ask you to click a link to unlock an account, share a code, or send money quickly. Do not tap the link. Do not type your password. If you are worried, call the company using the number on the back of your card.",
    safeCoaching:
      "This message looks normal. Still, never share passwords or one-time codes with anyone — not even someone who says they are from your bank.",
    whatThisMeans: "What this means",
    readAloud: "🔊 Read Warning Aloud",
    stopReading: "Stop Reading",
    whyFlagged: "Why we flagged this",
    languageLabel: "Voice language",
  },
  hi: {
    dangerHeading: "⚠️ खतरा",
    safeHeading: "✅ सुरक्षित",
    dangerTitle: "खतरा: धोखाधड़ी का पता चला",
    safeTitle: "सुरक्षित संदेश",
    cautionTitle: "सावधान रहें",
    cautionCoaching: "अभी कोई कदम न उठाएँ। पहले भरोसेमंद नंबर से पुष्टि करें और कोड या पासवर्ड साझा न करें।",
    dangerCoaching:
      "बैंक, सरकार या डिलीवरी कंपनियाँ कभी भी खाता खोलने के लिए लिंक पर क्लिक करने, कोड साझा करने या जल्दी पैसे भेजने के लिए नहीं कहेंगी। लिंक पर क्लिक न करें। अपना पासवर्ड न डालें। चिंता हो तो अपने कार्ड के पीछे लिखे नंबर पर बैंक को फोन करें।",
    safeCoaching:
      "यह संदेश सामान्य लगता है। फिर भी अपना पासवर्ड या ओटीपी किसी के साथ साझा न करें — भले ही वह कहे कि वह बैंक से है।",
    whatThisMeans: "इसका क्या मतलब है",
    readAloud: "🔊 चेतावनी सुनें",
    stopReading: "पढ़ना बंद करें",
    whyFlagged: "हमने इसे क्यों चिह्नित किया",
    languageLabel: "आवाज़ की भाषा",
  },
  ml: {
    dangerHeading: "⚠️ അപകടം",
    safeHeading: "✅ സുരക്ഷിതം",
    dangerTitle: "അപകടം: തട്ടിപ്പ് കണ്ടെത്തി",
    safeTitle: "സുരക്ഷിതമായ സന്ദേശം",
    cautionTitle: "ജാഗ്രത പാലിക്കുക",
    cautionCoaching: "ഇപ്പോൾ നടപടിയെടുക്കരുത്. നിങ്ങൾക്ക് അറിയാവുന്ന നമ്പറിൽ സ്ഥിരീകരിക്കുക; കോഡോ പാസ്‌വേഡോ പങ്കിടരുത്.",
    dangerCoaching:
      "ബാങ്കുകളോ സർക്കാരോ കൊറിയർ കമ്പനികളോ ഒരിക്കലും അക്കൗണ്ട് തുറക്കാൻ ലിങ്കിൽ അമർത്താനോ കോഡ് പങ്കുവയ്ക്കാനോ പെട്ടെന്ന് പണം അയയ്ക്കാനോ ആവശ്യപ്പെടില്ല. ലിങ്കിൽ അമർത്തരുത്. പാസ്‌വേഡ് ടൈപ്പ് ചെയ്യരുത്. സംശയമുണ്ടെങ്കിൽ കാർഡിന്റെ പിന്നിലുള്ള നമ്പറിൽ ബാങ്കിനെ വിളിക്കുക.",
    safeCoaching:
      "ഈ സന്ദേശം സാധാരണമായി തോന്നുന്നു. എങ്കിലും പാസ്‌വേഡോ ഒ.ടി.പി.യോ ആർക്കും പങ്കുവയ്ക്കരുത് — ബാങ്കിൽ നിന്നാണെന്ന് പറയുന്നവർക്കും.",
    whatThisMeans: "ഇതിന്റെ അർത്ഥം",
    readAloud: "🔊 മുന്നറിയിപ്പ് വായിക്കുക",
    stopReading: "വായന നിർത്തുക",
    whyFlagged: "എന്തുകൊണ്ട് ഇത് അപകടമായി കണ്ടു",
    languageLabel: "ശബ്ദ ഭാഷ",
  },
  kn: {
    dangerHeading: "⚠️ ಅಪಾಯ",
    safeHeading: "✅ ಸುರಕ್ಷಿತ",
    dangerTitle: "ಅಪಾಯ: ವಂಚನೆ ಪತ್ತೆಯಾಗಿದೆ",
    safeTitle: "ಸುರಕ್ಷಿತ ಸಂದೇಶ",
    cautionTitle: "ಜಾಗರೂಕರಾಗಿರಿ",
    cautionCoaching: "ಈಗ ಕ್ರಮ ಕೈಗೊಳ್ಳಬೇಡಿ. ನಿಮಗೆ ತಿಳಿದಿರುವ ಸಂಖ್ಯೆಯಲ್ಲಿ ದೃಢಪಡಿಸಿ; ಕೋಡ್ ಅಥವಾ ಪಾಸ್‌ವರ್ಡ್ ಹಂಚಿಕೊಳ್ಳಬೇಡಿ.",
    dangerCoaching:
      "ಬ್ಯಾಂಕುಗಳು, ಸರ್ಕಾರ ಅಥವಾ ಕೊರಿಯರ್ ಕಂಪನಿಗಳು ಖಾತೆ ತೆರೆಯಲು ಲಿಂಕ್ ಒತ್ತಲು, ಕೋಡ್ ಹಂಚಿಕೊಳ್ಳಲು ಅಥವಾ ತಕ್ಷಣ ಹಣ ಕಳುಹಿಸಲು ಎಂದಿಗೂ ಕೇಳುವುದಿಲ್ಲ. ಲಿಂಕ್ ಒತ್ತಬೇಡಿ. ಪಾಸ್‌ವರ್ಡ್ ಟೈಪ್ ಮಾಡಬೇಡಿ. ಸಂಶಯವಿದ್ದರೆ ಕಾರ್ಡ್ ಹಿಂಬದಿಯ ಸಂಖ್ಯೆಗೆ ಕರೆ ಮಾಡಿ.",
    safeCoaching:
      "ಈ ಸಂದೇಶ ಸಾಮಾನ್ಯವಾಗಿದೆ. ಆದರೂ ಪಾಸ್‌ವರ್ಡ್ ಅಥವಾ ಒಟಿಪಿ ಯಾರಿಗೂ ಹಂಚಿಕೊಳ್ಳಬೇಡಿ — ಬ್ಯಾಂಕಿನಿಂದ ಎಂದು ಹೇಳುವವರಿಗೂ ಸಹ.",
    whatThisMeans: "ಇದರ ಅರ್ಥ",
    readAloud: "🔊 ಎಚ್ಚರಿಕೆ ಓದಿಸಿ",
    stopReading: "ಓದುವುದನ್ನು ನಿಲ್ಲಿಸಿ",
    whyFlagged: "ನಾವು ಇದನ್ನು ಏಕೆ ಗುರುತಿಸಿದೆವು",
    languageLabel: "ಧ್ವನಿ ಭಾಷೆ",
  },
  ta: {
    dangerHeading: "⚠️ ஆபத்து",
    safeHeading: "✅ பாதுகாப்பானது",
    dangerTitle: "ஆபத்து: மோசடி கண்டறியப்பட்டது",
    safeTitle: "பாதுகாப்பான செய்தி",
    cautionTitle: "கவனமாக இருங்கள்",
    cautionCoaching: "இப்போது செயல்பட வேண்டாம். ஏற்கனவே நம்பும் எண்ணில் உறுதிப்படுத்துங்கள்; குறியீடு அல்லது கடவுச்சொல்லைப் பகிர வேண்டாம்.",
    dangerCoaching:
      "வங்கிகள், அரசு அல்லது கூரியர் நிறுவனங்கள் கணக்கைத் திறக்க இணைப்பை அழுத்தச் சொல்லவோ, குறியீட்டைப் பகிரச் சொல்லவோ, உடனே பணம் அனுப்பச் சொல்லவோ மாட்டார்கள். இணைப்பை அழுத்த வேண்டாம். கடவுச்சொல்லை உள்ளிட வேண்டாம். சந்தேகம் இருந்தால் அட்டையின் பின்புறம் உள்ள எண்ணுக்கு அழைக்கவும்.",
    safeCoaching:
      "இந்தச் செய்தி சாதாரணமாகத் தெரிகிறது. இருப்பினும் கடவுச்சொல் அல்லது OTP-ஐ யாருடனும் பகிர வேண்டாம் — வங்கியிலிருந்து என்று சொல்பவருடன் கூட.",
    whatThisMeans: "இதன் பொருள்",
    readAloud: "🔊 எச்சரிக்கையைக் கேளுங்கள்",
    stopReading: "வாசிப்பை நிறுத்து",
    whyFlagged: "ஏன் இதைக் குறித்தோம்",
    languageLabel: "குரல் மொழி",
  },
  te: {
    dangerHeading: "⚠️ ప్రమాదం",
    safeHeading: "✅ సురక్షితం",
    dangerTitle: "ప్రమాదం: మోసం గుర్తించబడింది",
    safeTitle: "సురక్షిత సందేశం",
    cautionTitle: "జాగ్రత్తగా ఉండండి",
    cautionCoaching: "ఇప్పుడే ఏమీ చేయవద్దు. మీకు తెలిసిన నంబర్‌తో నిర్ధారించండి; కోడ్ లేదా పాస్‌వర్డ్ పంచుకోవద్దు.",
    dangerCoaching:
      "బ్యాంకులు, ప్రభుత్వం లేదా కొరియర్ సంస్థలు ఖాతా తెరవడానికి లింక్ నొక్కమని, కోడ్ పంచుకోమని లేదా వెంటనే డబ్బు పంపమని ఎప్పుడూ అడగవు. లింక్ నొక్కవద్దు. పాస్‌వర్డ్ టైప్ చేయవద్దు. అనుమానం ఉంటే కార్డు వెనుక ఉన్న నంబర్‌కు ఫోన్ చేయండి.",
    safeCoaching:
      "ఈ సందేశం సాధారణంగా ఉంది. అయినా పాస్‌వర్డ్ లేదా OTP ఎవరితోనూ పంచుకోవద్దు — బ్యాంకు నుంచి అని చెప్పేవారితో కూడా.",
    whatThisMeans: "దీని అర్థం",
    readAloud: "🔊 హెచ్చరిక వినండి",
    stopReading: "చదవడం ఆపు",
    whyFlagged: "మేము దీన్ని ఎందుకు గుర్తించాం",
    languageLabel: "వాయిస్ భాష",
  },
  bn: {
    dangerHeading: "⚠️ বিপদ",
    safeHeading: "✅ নিরাপদ",
    dangerTitle: "বিপদ: প্রতারণা শনাক্ত হয়েছে",
    safeTitle: "নিরাপদ বার্তা",
    cautionTitle: "সাবধানে থাকুন",
    cautionCoaching: "এখনই কিছু করবেন না। আগে থেকে জানা নম্বরে যাচাই করুন, আর কোড বা পাসওয়ার্ড দেবেন না।",
    dangerCoaching:
      "ব্যাংক, সরকার বা কুরিয়ার সংস্থা কখনোই অ্যাকাউন্ট খুলতে লিঙ্কে ক্লিক করতে, কোড শেয়ার করতে বা দ্রুত টাকা পাঠাতে বলবে না। লিঙ্কে ক্লিক করবেন না। পাসওয়ার্ড লিখবেন না। সন্দেহ হলে কার্ডের পেছনের নম্বরে ফোন করুন।",
    safeCoaching:
      "এই বার্তাটি স্বাভাবিক মনে হচ্ছে। তবুও পাসওয়ার্ড বা ওটিপি কারও সঙ্গে শেয়ার করবেন না — ব্যাংক থেকে বলা কেউ হলেও নয়।",
    whatThisMeans: "এর মানে কী",
    readAloud: "🔊 সতর্কতা শুনুন",
    stopReading: "পড়া বন্ধ করুন",
    whyFlagged: "কেন আমরা এটি চিহ্নিত করেছি",
    languageLabel: "কণ্ঠের ভাষা",
  },
  mr: {
    dangerHeading: "⚠️ धोका",
    safeHeading: "✅ सुरक्षित",
    dangerTitle: "धोका: फसवणूक आढळली",
    safeTitle: "सुरक्षित संदेश",
    cautionTitle: "काळजी घ्या",
    cautionCoaching: "आत्ता काही करू नका. आधीपासून माहीत असलेल्या क्रमांकावर खात्री करा आणि कोड किंवा पासवर्ड सांगू नका.",
    dangerCoaching:
      "बँका, सरकार किंवा कुरिअर कंपन्या कधीही खाते उघडण्यासाठी लिंकवर क्लिक करायला, कोड शेअर करायला किंवा लगेच पैसे पाठवायला सांगत नाहीत. लिंकवर क्लिक करू नका. पासवर्ड टाकू नका. शंका असल्यास कार्डाच्या मागील क्रमांकावर फोन करा.",
    safeCoaching:
      "हा संदेश सामान्य वाटतो. तरीही पासवर्ड किंवा ओटीपी कोणालाही सांगू नका — बँकेतून असल्याचे सांगणाऱ्यालाही नाही.",
    whatThisMeans: "याचा अर्थ",
    readAloud: "🔊 इशारा ऐका",
    stopReading: "वाचन थांबवा",
    whyFlagged: "आम्ही हे का चिन्हांकित केले",
    languageLabel: "आवाजाची भाषा",
  },
  gu: {
    dangerHeading: "⚠️ ભય",
    safeHeading: "✅ સુરક્ષિત",
    dangerTitle: "ભય: છેતરપિંડી મળી",
    safeTitle: "સુરક્ષિત સંદેશ",
    cautionTitle: "સાવચેત રહો",
    cautionCoaching: "હમણાં કંઈ ન કરો. પહેલેથી જાણતા નંબર પર ખાતરી કરો અને કોડ કે પાસવર્ડ આપશો નહીં.",
    dangerCoaching:
      "બેંક, સરકાર કે કુરિયર કંપની ક્યારેય ખાતું ખોલવા લિંક દબાવવા, કોડ શેર કરવા કે તરત પૈસા મોકલવા કહેશે નહીં. લિંક દબાવશો નહીં. પાસવર્ડ ટાઈપ કરશો નહીં. શંકા હોય તો કાર્ડની પાછળના નંબર પર ફોન કરો.",
    safeCoaching:
      "આ સંદેશ સામાન્ય લાગે છે. તેમ છતાં પાસવર્ડ કે OTP કોઈને આપશો નહીં — બેંકમાંથી હોવાનું કહેનારને પણ નહીં.",
    whatThisMeans: "આનો અર્થ",
    readAloud: "🔊 ચેતવણી સાંભળો",
    stopReading: "વાંચવાનું બંધ કરો",
    whyFlagged: "અમે આને કેમ ચિહ્નિત કર્યું",
    languageLabel: "અવાજની ભાષા",
  },
  pa: {
    dangerHeading: "⚠️ ਖ਼ਤਰਾ",
    safeHeading: "✅ ਸੁਰੱਖਿਅਤ",
    dangerTitle: "ਖ਼ਤਰਾ: ਧੋਖਾਧੜੀ ਮਿਲੀ",
    safeTitle: "ਸੁਰੱਖਿਅਤ ਸੁਨੇਹਾ",
    cautionTitle: "ਸਾਵਧਾਨ ਰਹੋ",
    cautionCoaching: "ਹੁਣੇ ਕੋਈ ਕਦਮ ਨਾ ਚੁੱਕੋ। ਜਾਣੇ-ਪਛਾਣੇ ਨੰਬਰ ਤੋਂ ਪੱਕਾ ਕਰੋ ਅਤੇ ਕੋਡ ਜਾਂ ਪਾਸਵਰਡ ਨਾ ਦਿਓ।",
    dangerCoaching:
      "ਬੈਂਕ, ਸਰਕਾਰ ਜਾਂ ਕੋਰੀਅਰ ਕੰਪਨੀਆਂ ਕਦੇ ਵੀ ਖਾਤਾ ਖੋਲ੍ਹਣ ਲਈ ਲਿੰਕ ਦਬਾਉਣ, ਕੋਡ ਸਾਂਝਾ ਕਰਨ ਜਾਂ ਤੁਰੰਤ ਪੈਸੇ ਭੇਜਣ ਲਈ ਨਹੀਂ ਕਹਿੰਦੀਆਂ। ਲਿੰਕ ਨਾ ਦਬਾਓ। ਪਾਸਵਰਡ ਨਾ ਪਾਓ। ਸ਼ੱਕ ਹੋਵੇ ਤਾਂ ਕਾਰਡ ਦੇ ਪਿੱਛੇ ਲਿਖੇ ਨੰਬਰ 'ਤੇ ਫ਼ੋਨ ਕਰੋ।",
    safeCoaching:
      "ਇਹ ਸੁਨੇਹਾ ਆਮ ਲੱਗਦਾ ਹੈ। ਫਿਰ ਵੀ ਪਾਸਵਰਡ ਜਾਂ OTP ਕਿਸੇ ਨਾਲ ਸਾਂਝਾ ਨਾ ਕਰੋ।",
    whatThisMeans: "ਇਸਦਾ ਮਤਲਬ",
    readAloud: "🔊 ਚੇਤਾਵਨੀ ਸੁਣੋ",
    stopReading: "ਪੜ੍ਹਨਾ ਬੰਦ ਕਰੋ",
    whyFlagged: "ਅਸੀਂ ਇਸਨੂੰ ਕਿਉਂ ਨਿਸ਼ਾਨਬੱਧ ਕੀਤਾ",
    languageLabel: "ਆਵਾਜ਼ ਦੀ ਭਾਸ਼ਾ",
  },
  ur: {
    dangerHeading: "⚠️ خطرہ",
    safeHeading: "✅ محفوظ",
    dangerTitle: "خطرہ: فراڈ کا پتہ چلا",
    safeTitle: "محفوظ پیغام",
    cautionTitle: "احتیاط کریں",
    cautionCoaching: "ابھی کوئی قدم نہ اٹھائیں۔ پہلے سے معلوم نمبر پر تصدیق کریں اور کوڈ یا پاس ورڈ نہ بتائیں۔",
    dangerCoaching:
      "بینک، حکومت یا کوریئر کمپنیاں کبھی بھی اکاؤنٹ کھولنے کے لیے لنک دبانے، کوڈ شیئر کرنے یا فوراً پیسے بھیجنے کو نہیں کہتیں۔ لنک پر کلک نہ کریں۔ پاس ورڈ نہ لکھیں۔ شک ہو تو کارڈ کے پیچھے لکھے نمبر پر فون کریں۔",
    safeCoaching:
      "یہ پیغام عام لگتا ہے۔ پھر بھی پاس ورڈ یا OTP کسی کو نہ بتائیں۔",
    whatThisMeans: "اس کا مطلب",
    readAloud: "🔊 انتباہ سنیں",
    stopReading: "پڑھنا بند کریں",
    whyFlagged: "ہم نے اسے کیوں نشان زد کیا",
    languageLabel: "آواز کی زبان",
  },
  es: {
    dangerHeading: "⚠️ PELIGRO",
    safeHeading: "✅ SEGURO",
    dangerTitle: "PELIGRO: ESTAFA DETECTADA",
    safeTitle: "Mensaje seguro",
    cautionTitle: "Tenga cuidado",
    cautionCoaching: "No actúe todavía. Confirme usando un número que ya conozca y nunca comparta códigos ni contraseñas.",
    dangerCoaching:
      "Los bancos, el gobierno y las empresas de entrega nunca le pedirán hacer clic en un enlace para desbloquear una cuenta, compartir un código o enviar dinero con urgencia. No toque el enlace. No escriba su contraseña. Si tiene dudas, llame al número que aparece en su tarjeta.",
    safeCoaching:
      "Este mensaje parece normal. Aun así, nunca comparta contraseñas ni códigos de un solo uso con nadie.",
    whatThisMeans: "Qué significa esto",
    readAloud: "🔊 Leer la advertencia",
    stopReading: "Detener lectura",
    whyFlagged: "Por qué lo marcamos",
    languageLabel: "Idioma de voz",
  },
};

export function t(lang: LangCode): Strings {
  return TRANSLATIONS[lang] ?? TRANSLATIONS.en;
}
