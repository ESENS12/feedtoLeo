# 레오에게 밥주기 귀찮아서 만든 프로젝트
강아지 자동 급식기 프로젝트의 안드로이드 클라이언트 모듈, 아두이노 코드는 간단하므로 별도로 관리하지 않음

# 사용기술
- Arduino
- Android
- Firebase

# 아두이노+모터+블루투스로 구성된 급식기
- firebase-Realtime-database를 활용한 아주 간단한 원격 급식기
- 서버to모듈간 데이터전송을 담당할 안드로이드 ClientApp과
- 그냥 서버로 데이터만 전송하는 실사용 ClientApp으로 구성
- 외부에서 데이터로 급식을 줘야 하기 때문에, 집 내부에 연결되어있는 홈캠용 안드로이드 디바이스를 활용함
