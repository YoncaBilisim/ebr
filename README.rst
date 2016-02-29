ebr
===

.. image:: https://travis-ci.org/YoncaBilisim/ebr.svg?branch=master
    :target: https://travis-ci.org/YoncaBilisim/ebr

East Blue Reporting
-------------------

UYARI :: Uygulama şu anda geliştirme aşamasındadır.

Amacımız loglanabilir, versiyonlanabilir hafif bir raporlama aracı. İleride eklemeyi düşündüğümüz eklenti (plugin) desteği ile farklı özellikler de ilave edilebilir.

Kurulum
-------
Uygulamayı kurmak gayet basittir. Ayarların hepsini tek bir dosyadan (ebr.conf) yapabilirsiniz. Bu dosyada yaptığınız değişiklikler anında aktif olur.

Ön hazırlık
~~~~~~~~~~~
 * Sunucu : Bir linux sistem üzerine kurmanız tavsiye edilir. Windows üzerinde henüz test edilmedi. Ancak test eder ve bir problem ile karşılaşırsanız problemi kendiniz çözüp github üzerinden bize gönderebilirsiniz. Kendiniz çözemediğiniz durumlarda github üzerinden bug bildirimi yaparsanız problemi çözmekten zevk alırız.
 * JDK8 : projemiz java 8 ortamında çalışıyor. Oracle jdk ile test edildi ancak OpenJDK veya diğerleri test edilmedi.
 * Tomcat 8 veya başka bir servet desteği olan bir sunucu.

Ayarlar
~~~~~~~
Kullanıcınızın home (ev) dizinine ebr.conf dosyasını https://gist.github.com/myururdurmaz/47c42ec6d0f00a096b97 adresinden indirebilirsiniz::
    linux   : /home/kullanici/ebr.conf
    windows : c:\users\kullanici\ebr.conf 

Deploy
~~~~~~
Tomcat kurulumundan sonra war dosyasını webapps dizinine attığınızda tomcat açık ise otomatik deploy olacaktır.

...

Bu uygulamayı kullanmanızdan oluşacak durumlar Yonca Bilişim Teknolojileri Ltd. Şirketini bir sorumluluk altına sokmaz.
Uygulamanın tüm hakları Yonca Bilişim Teknolojileri Ltd. Şirketine aittir.
Bu uygulamayı alıp istediğiniz gibi geliştirebilir, test edebilir, kullanabilirsiniz. Ancak Uygulama ismini değiştiremez ve farklı bir isim ile yayınlayamazsınız.
