# LogFlow Architecture

## Genel Bakış

LogFlow projesi, verinin bir kaynaktan alınarak bir işlem hattı üzerinden hedef bileşene aktarılması mantığıyla tasarlanmıştır.

Increment 1 kapsamında sistemin temel amacı, bir log dosyasını satır satır okuyup konsola yazdırmaktır.

Bu işlem doğrudan Main sınıfı içerisinde yapılmaz. Bunun yerine sorumluluklar farklı bileşenlere ayrılmıştır.

## Temel Mimari

```text
+--------------------+        +--------------------+
|   FileLineSource   | -----> |    ConsoleSink     |
|      Source        |        |       Sink         |
+--------------------+        +--------------------+
          \____________________________/
                       |
                    Pipeline