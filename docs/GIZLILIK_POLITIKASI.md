# Gizlilik Politikası

**Son güncelleme:** 26 Mayıs 2026

Bu Gizlilik Politikası, **KTS – Koçluk Takip Sistemi** mobil uygulamasının (“Uygulama”) kişisel verilerinizi nasıl topladığını, kullandığını, sakladığını ve koruduğunu açıklar. Uygulamayı indirerek, kayıt olarak veya kullanmaya devam ederek bu politikayı okuduğunuzu ve kabul ettiğinizi beyan etmiş olursunuz.

---

## 1. Veri Sorumlusu

| | |
|---|---|
| **Uygulama** | KTS – Koçluk Takip Sistemi |
| **Paket adı** | `com.karaketir.coachingapp` |
| **Geliştirici** | Furkan Karaketir |
| **İletişim** | Gizlilik talepleri için: [e-posta adresinizi buraya ekleyin] |

6698 sayılı Kişisel Verilerin Korunması Kanunu (“KVKK”) kapsamında veri sorumlusu, yukarıda belirtilen geliştiricidir.

---

## 2. Uygulamanın Amacı

KTS, öğrencilerin ve öğretmenlerin (koçların) aynı kurum kodu altında bir araya gelerek:

- ders çalışması ve soru çözümü kayıtlarını tutmasını,
- görev ve hedefleri yönetmesini,
- performans istatistikleri ve değerlendirmeleri görüntülemesini,
- kurum içi bildirimler almasını

sağlayan bir **eğitim ve koçluk takip** uygulamasıdır.

---

## 3. Toplanan Veriler

Uygulama kullanımınıza bağlı olarak aşağıdaki veri kategorileri işlenebilir:

### 3.1. Hesap ve kimlik bilgileri

- **Ad ve soyad**
- **E-posta adresi**
- **Şifre** (doğrudan saklanmaz; Google Firebase Authentication altyapısında şifrelenmiş/karma biçimde işlenir)
- **Kullanıcı kimliği (UID)** — Firebase tarafından atanan benzersiz tanımlayıcı
- **Kullanıcı türü** — Öğrenci (“Student”) veya Öğretmen (“Teacher”)

### 3.2. Kurum ve eğitim profili bilgileri

- **Kurum kodu** (`kurumKodu`)
- **Sınıf** (öğrenciler için)
- **Alan / branş** (ör. Sayısal, Sözel, Matematik, Türk Dili ve Edebiyatı vb.)
- **Atanmış koç öğretmen bilgisi** (öğrenci–öğretmen eşleştirmesi)

### 3.3. Akademik ve kullanım verileri

Uygulama işlevselliği kapsamında Firebase Firestore veritabanında saklanan veriler:

| Veri türü | Örnek içerik |
|---|---|
| **Çalışma kayıtları** | Ders adı, konu/tema, çalışma türü, çözülen soru sayısı, toplam çalışma süresi, tarih/saat |
| **Görevler** | Atanan konular, bitiş zamanı, tamamlanma durumu |
| **Haftalık hedefler** | Ders bazlı hedefler |
| **Değerlendirmeler** | Yıldız puanı, değerlendirme tarihi |
| **Ders programı** | Günlük ders planı |
| **Rapor / devamsızlık** | Rapor verilmeyen gün sayıları |
| **Mesajlaşma bildirimleri** | Bildirim başlığı ve metni (push bildirimi olarak iletilir; kalıcı mesajlaşma geçmişi tutulmaz) |

### 3.4. Teknik ve cihaz verileri

Aşağıdaki hizmetler aracılığıyla otomatik olarak toplanabilecek veriler:

- **Firebase Cloud Messaging (FCM)** — bildirim abonelik konuları (kurum kodu, koç kimliği, kullanıcı kimliği) ve cihaz bildirim jetonu
- **Firebase Analytics** — uygulama kullanım istatistikleri (anonimleştirilmiş/toplulaştırılmış olabilir)
- **Firebase Crashlytics** — çökme raporları, hata günlükleri, cihaz modeli, işletim sistemi sürümü
- **Firebase Performance Monitoring** — uygulama performans metrikleri
- **Wi-Fi durumu** — ağ bağlantısı kontrolü için

Bu veriler, uygulamanın kararlı çalışması, hata giderme ve iyileştirme amacıyla işlenir.

### 3.5. Toplanmayan veya isteğe bağlı veriler

- Uygulama, kayıt sırasında **yalnızca e-posta ve şifre** ile kimlik doğrulaması kullanır; telefon numarası zorunlu değildir.
- **Konum verisi** toplanmaz.
- **Kamera** donanımı isteğe bağlıdır (`android:required="false"`); profil fotoğrafı yükleme gibi özellikler kullanılmadığı sürece kamera erişimi talep edilmez.
- **Harici depolama** izni, yalnızca istatistik ve çalışma verilerini **Excel dosyası olarak dışa aktarma** gibi özelliklerde kullanılır.

---

## 4. Verilerin Toplanma Yöntemleri

Veriler şu yollarla toplanır:

1. **Doğrudan sizden** — kayıt formu, profil düzenleme, çalışma/görev girişi, mesaj gönderme
2. **Otomatik olarak** — oturum açma, bildirim abonelikleri, analitik ve hata izleme servisleri
3. **Öğretmen / kurum yöneticisi aracılığıyla** — öğrenci değerlendirmeleri, görev atamaları (kurum içi yetkilendirme ile)

---

## 5. Verilerin İşlenme Amaçları

Kişisel verileriniz aşağıdaki amaçlarla işlenir:

| Amaç | Hukuki dayanak (KVKK m. 5) |
|---|---|
| Hesap oluşturma ve kimlik doğrulama | Sözleşmenin kurulması ve ifası |
| Koçluk ve eğitim takip hizmetinin sunulması | Sözleşmenin ifası |
| Kurum içi bildirim gönderimi | Sözleşmenin ifası / meşru menfaat |
| Uygulama güvenliği ve hata analizi | Meşru menfaat |
| Yasal yükümlülüklerin yerine getirilmesi | Kanunlarda öngörülmesi |
| İstatistik ve performans iyileştirme | Meşru menfaat |

Açık rızanız olmadan reklam profili oluşturmak veya verilerinizi üçüncü taraflara satmak amacıyla işlem yapılmaz.

---

## 6. Verilerin Paylaşılması ve Üçüncü Taraflar

Verileriniz, hizmetin sunulması için aşağıdaki üçüncü taraf sağlayıcılarla paylaşılabilir:

| Sağlayıcı | Hizmet | Politika |
|---|---|---|
| **Google LLC (Firebase)** | Kimlik doğrulama, veritabanı (Firestore), dosya depolama, analitik, çökme raporlama, performans izleme, push bildirimleri | [Firebase Gizlilik](https://firebase.google.com/support/privacy) |
| **Google LLC (AdMob)** | Uygulama içi reklamlar (yapılandırılmışsa) | [Google Reklam Gizliliği](https://policies.google.com/technologies/ads) |
| **Giphy, Inc.** | GIF/medya SDK yapılandırması (kullanıldığında) | [Giphy Gizlilik](https://giphy.com/privacy) |

Bu sağlayıcılar, verileri kendi gizlilik politikalarına uygun olarak işler. Firebase altyapısı nedeniyle verileriniz **Türkiye dışındaki sunucularda** (ör. Avrupa Birliği veya Amerika Birleşik Devletleri) saklanabilir ve işlenebilir.

Kurum kodunuz altındaki **aynı okul/kurumdaki yetkili öğretmenler**, öğrenci performans verilerine uygulama içi yetkilendirme kapsamında erişebilir.

---

## 7. Bildirimler (Push Notifications)

Uygulama, Firebase Cloud Messaging kullanarak:

- size atanan görev ve çalışma hatırlatmalarını,
- öğretmeninizden veya kurumunuzdan gelen duyuruları

cihazınıza iletebilir. Bildirim almak için Android’in **bildirim izni** istenebilir. İzni istediğiniz zaman cihaz ayarlarından kapatabilirsiniz.

FCM konu abonelikleri, kurum kodunuz ve (öğrenci iseniz) koç öğretmen kimliğinize göre otomatik oluşturulur.

---

## 8. Çerezler ve Benzer Teknolojiler

Mobil uygulama geleneksel web çerezleri kullanmaz. Bununla birlikte **Firebase Analytics**, **Crashlytics**, **Performance** ve (etkinse) **Google AdMob** gibi hizmetler; cihaz tanımlayıcıları, kullanım oturumu ve reklam kimlikleri gibi **benzer izleme teknolojileri** kullanabilir. Bu teknolojilerin amacı uygulama performansını ölçmek, hataları gidermek ve (varsa) reklam sunmaktır.

Reklam kişiselleştirmesini Android ayarlarından **“Reklamlar” → “Reklam kişiselleştirmesini devre dışı bırak”** seçeneğiyle sınırlayabilirsiniz.

---

## 9. Veri Saklama Süresi

- Hesabınız aktif olduğu sürece verileriniz Firebase altyapısında saklanır.
- **Hesap silme** işlemini Uygulama içindeki Ayarlar bölümünden gerçekleştirdiğinizde; kullanıcı profiliniz, Firebase Authentication kaydınız ve ilişkili okul/kurum kayıtlarınız silinmeye çalışılır.
- Analitik ve çökme raporu verileri, Google’ın saklama politikalarına tabidir ve genellikle belirli bir süre sonra anonimleştirilir veya silinir.
- Yasal zorunluluklar (ör. vergi, uyuşmazlık) saklama süresini uzatabilir.

---

## 10. Veri Güvenliği

Verilerinizin korunması için:

- **HTTPS / şifreli bağlantı** üzerinden iletişim,
- **Firebase Authentication** ile güvenli oturum yönetimi,
- Google Firebase altyapısının sağladığı sunucu tarafı güvenlik önlemleri

kullanılmaktadır. Hiçbir sistem %100 güvenli olmadığından mutlak güvenlik garanti edilemez; ancak makul teknik ve idari tedbirler alınmaktadır.

Cihazınızda otomatik yedekleme (`allowBackup`) etkinse, bazı uygulama verileri Google/Android yedekleme hizmetlerine dahil olabilir.

---

## 11. Haklarınız (KVKK m. 11)

Kişisel veri sahibi olarak aşağıdaki haklara sahipsiniz:

1. Kişisel verilerinizin işlenip işlenmediğini öğrenme  
2. İşlenmişse buna ilişkin bilgi talep etme  
3. İşlenme amacını ve amacına uygun kullanılıp kullanılmadığını öğrenme  
4. Yurt içinde veya yurt dışında aktarıldığı üçüncü kişileri bilme  
5. Eksik veya yanlış işlenmişse düzeltilmesini isteme  
6. KVKK m. 7 kapsamında silinmesini veya yok edilmesini isteme  
7. Düzeltme/silme işlemlerinin aktarıldığı üçüncü kişilere bildirilmesini isteme  
8. Münhasıran otomatik sistemlerle analiz edilmesi suretiyle aleyhinize bir sonucun ortaya çıkmasına itiraz etme  
9. Kanuna aykırı işlenmesi sebebiyle zarara uğramanız hâlinde zararın giderilmesini talep etme  

Taleplerinizi **[e-posta adresinizi buraya ekleyin]** adresine iletebilirsiniz. Başvurularınız, KVKK’da öngörülen süreler içinde yanıtlanacaktır.

Profil bilgilerinizi Uygulama içinden güncelleyebilir; hesabınızı Ayarlar → **Hesabı Sil** ile silebilirsiniz.

---

## 12. Çocukların Gizliliği

Uygulama, ortaöğretim ve sınav hazırlığı öğrencilerinin kullanımına yöneliktir. **18 yaşın altındaki** kullanıcıların Uygulamayı kullanabilmesi için veli veya yasal vasinin bilgisi ve onayı önerilir. Veli/vasi olarak çocuğunuzun verilerinin silinmesini veya düzeltilmesini talep edebilirsiniz.

---

## 13. Üçüncü Taraf Bağlantıları

Uygulama, geliştirici LinkedIn profili ve Google Play mağaza sayfası gibi harici bağlantılar açabilir. Bu sitelerin gizlilik uygulamalarından Uygulama sorumlu değildir.

---

## 14. Politika Değişiklikleri

Bu Gizlilik Politikası zaman zaman güncellenebilir. Önemli değişikliklerde Uygulama içi bildirim veya mağaza açıklaması yoluyla bilgilendirme yapılabilir. Güncel metin her zaman bu belgede yayımlanır; değişiklikten sonra Uygulamayı kullanmaya devam etmeniz, güncellenmiş politikayı kabul ettiğiniz anlamına gelir.

---

## 15. İletişim

Gizlilik ile ilgili soru, talep veya şikâyetleriniz için:

- **E-posta:** [e-posta adresinizi buraya ekleyin]  
- **Geliştirici:** Furkan Karaketir  
- **Play Store:** [KTS – Koçluk Takip Sistemi](https://play.google.com/store/apps/details?id=com.karaketir.coachingapp)

---

*Bu belge, Uygulamanın mevcut teknik yapısına (Firebase, FCM, Analytics, Crashlytics, AdMob yapılandırması vb.) dayanarak hazırlanmıştır. Hukuki danışmanlık yerine geçmez; yayınlamadan önce bir hukuk uzmanına gözden geçirtmeniz önerilir.*
