# README
![Scheme](https://firebasestorage.googleapis.com/v0/b/prohardver-45e92.appspot.com/o/read_me_showcase.png?alt=media&token=9f7899ec-d976-4713-b07b-b2cebaf83539)
# PH!
 
*A [Prohardver](https://prohardver.hu/index.html) hírportál mobilalkalmazása*

### Támogatott eszközök és SDK

* Android 5.0 vagy a felett (minSDK 21)
* Jelenleg tabletekre nincs optimalizálva

### Kapcsolat

* [Prohardver topik figyelő](http://prohardver.hu/tema/prohardver_topik_figyelo_android_alkalmazas/friss.html)

### Kérlek olvasd el, mielőtt módosítanál valamit!

* Az alkalmazásban a `Log` osztály helyett a `PHApplication.getInstance().loginfo()` és `PHApplication.getInstance().logError()` metódusokat használd!
* Az `AndroidManifest.xml`-ben add meg a fabric kulcsodat, különdben nem fog menni az app
* Ugyanúgy a `Firebase`-n hozz létre egy projektet, majd a `google-services.json` fájlt másold a `/app` könyvtárba

Ezt követően mennie kell az appnak ha futtatod