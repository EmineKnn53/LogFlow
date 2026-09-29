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