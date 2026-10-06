EMİNE KENAN - 2404040243

# LogFlow

LogFlow, Yazılım Mimarisi dersi kapsamında geliştirilen basit bir pipeline tabanlı log işleme uygulamasıdır.

Bu ilk sürümde sistem bir metin dosyasını satır satır okur ve her satırı pipeline üzerinden konsola gönderir.

## Projenin Amacı

Projenin amacı, dosya okuma ve çıktı verme işlemlerini doğrudan Main sınıfında yapmak yerine sorumlulukları farklı bileşenlere ayırarak pipeline mimarisinin temelini oluşturmaktır.

Sistem temel olarak şu akışla çalışır:

FileLineSource -> Pipeline -> ConsoleSink

## Temel Bileşenler

### Source

Sisteme veri sağlayan bileşeni tanımlar.

Bu projede `FileLineSource`, verilen log dosyasını satır satır okuyarak her satırı sisteme gönderir.

### Emitter

Bir bileşenin ürettiği veriyi pipeline içerisindeki sonraki bileşene iletir.

### Stage

Pipeline içerisinde veriyi işleyebilecek aşamaları tanımlar.

Bir Stage, bir girdiden sıfır, bir veya birden fazla çıktı üretebilir.

### Sink

Pipeline sonunda veriyi tüketen bileşeni tanımlar.

Bu projede `ConsoleSink`, gelen log satırlarını konsola yazdırır.

### Pipeline

Source, Stage ve Sink bileşenleri arasındaki veri akışını yönetir.

## Proje Yapısı

```text
LogFlow/
├── data/
│   └── access-small.log
├── src/
│   ├── ConsoleSink.java
│   ├── Emitter.java
│   ├── FileLineSource.java
│   ├── Main.java
│   ├── Pipeline.java
│   ├── Sink.java
│   ├── Source.java
│   ├── Stage.java
│   └── StageException.java
├── ARCHITECTURE.md
└── README.md




# LogFlow – Artım 2

Bu sürümde LogFlow projesine yapılandırılmış log kayıtları ve Common Log Format (CLF) ayrıştırma desteği eklenmiştir. İşlem hattı artık yalnızca String verileri taşımak yerine LogRecord alan modelini kullanabilmektedir.

## Yapılan Değişiklikler

- Değişmez (immutable) LogRecord alan modeli eklendi.
- Common Log Format kayıtlarını ayrıştırmak için ParserStage geliştirildi.
- LogRecord nesnelerini okunabilir tek satırlık biçimde göstermek için ConsoleSink güncellendi.
- Pipeline, String girdilerini ParserStage üzerinden LogRecord nesnelerine dönüştürecek şekilde güncellendi.
- ParserStage için birim testleri oluşturuldu.
- Testlerde dosya sistemine bağımlı olmamak için CollectingEmitter test double kullanıldı.
- Pipeline için ek bir birim testi oluşturuldu.

## Testler

JUnit kullanılarak toplam 9 birim testi çalıştırılmıştır.

- ParserStageTest: 8 test
- PipelineTest: 1 test
- Başarılı test: 9
- Başarısız test: 0

ParserStage testlerinde geçerli satır, eksik alan, hatalı zaman damgası, hatalı durum kodu, boş satır, fazladan boşluk, tırnaklı user-agent ve query string içeren satır senaryoları kontrol edilmiştir.

## Kod Kapsama

Kod kapsama ölçümü JaCoCo kullanılarak gerçekleştirilmiştir.

- Line Coverage: %76,24
- Instruction Coverage: %78
- Branch Coverage: %62

## Çalıştırma

Program örnek log dosyası ile çalıştırılabilir:

```text
logflow data/access-small.log