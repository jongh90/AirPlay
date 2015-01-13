Class AirplayService

About
해당 class는 JmDNS 라이브러리를 이용하여 안드로이드 어플리케이션에서 bonjour를 사용하는 appleTV를 인식하고 연결하여 airplay를 사용하도록 한다.  airplay를 통해서는 이미지를 전송하고 보여주며 특히 캐싱을 통해서 5장까지 이미지를 전송하여 계속해서 보여주는 것이 가능하다. 


Performance
- 이미지 전송속도 : 1~1.5 MB/s (캐싱시 0.5~1 MB/s)
- 캐싱된 이미지 출력 : 크기 상관없이 1초 이내 출력 
- 최대 전송 크기 : 제한없음 (30 MB 까지 테스트)
- 최대 캐싱 크기 : 약 10MB (초과시 캐싱하지 않고 전송 후 바로 표시함)
- 최대 캐싱 개수 : 5 개 (용량상관 없음)

Limitation
- 슬라이드쇼, 비디오, 오디오 출력 

Implement
public class AirplayService implements ServiceListener

Permission
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" /> 
<uses-permission android:name="android.permission.CHANGE_WIFI_MULTICAST_STATE" />
<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
<uses-permission android:name="android.permission.ACCESS_WIFI_STATE" /> 

Imported Library
jmdns-3.4.0.jar
    
Implements Interfaces
ServiceListener

Method Summary
public void connect(InetAddress deviceAddress)
apple TV 기기들을 인식하고 그 목록을 Map<String, ServiceInfo> services에 저장한다.
deviceAddress - 안드로이드 기기 wifi의 ip address

public void stop()
JmDNS를 종료한다. 반드시 호출하지 않아도 된다.

public void putImage(File file, String transition)
apple TV에 이미지를 전송한다.
file - file type의 이미지
transition - 전송된 이미지가 나타날 방식 (Dissolve, SlideLeft, SlideRight)

public void stopImage()
apple TV에 보이는 이미지를 중지시키며 화면으로 복귀된다.

public void putCacheImage(String key, File file)
key 값을 가지고 이미지 파일을 appleTV의 캐시에 저장한다.
key - UID 형태의 string 숫자
file - file tpye의 이미지

public void showCacheImage(String key, String transition)
appleTV 캐시에 저장되있고 key 값과 일치하는 이미지를 표시한다.
key - UID 형태의 string 숫자
transition - 전송된 이미지가 나타날 방식 (Dissolve, SlideLeft, SlideRight)

public Map<String, ServiceInfo> getServices()
현재 네트워크상에 인식된 appleTV 기기들의 정보를 가져온다.


Reference
AirPlay Protocol Specification : http://nto.github.io/AirPlay
JmDNS : http://jmdns.sourceforge.net/
droidPlay : https://github.com/tutikka/DroidPlay




