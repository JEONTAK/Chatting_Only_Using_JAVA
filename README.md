# Chatting Only Using JAVA

## 자바를 사용한 채팅 프로그램

프로젝트 소개 : https://youtu.be/9NSnzEFeMhs

## 프로젝트 개요

이 프로젝트는 **실시간 채팅 애플리케이션**으로, Java를 사용해 사용자들이 회원가입/로그인, 친구 관리, 1:1 및 그룹 채팅 기능을 이용할 수 있도록 구현되었습니다. 클라이언트-서버 아키텍처를 기반으로 하며,
MySQL 데이터베이스를 통해 데이터를 저장하고, RSA 암호화를 통해 비밀번호 보안을 강화했습니다. Java Swing으로 GUI를 제공하며, 공공 API를 활용해 일일 코로나19 통계를 표시하는 부가 기능도
포함합니다.

### 목적

사용자들이 다음 기능을 수행할 수 있는 플랫폼 제공

- 안전한 회원가입 및 로그인
- 친구 추가/삭제 및 친구 목록 관리
- 1:1 및 그룹 채팅방 생성/참여
- "오늘의 한마디" 상태 메시지 업데이트
- 실시간 사용자 온라인/오프라인 상태 확인
- 일일 코로나19 확진자/사망자 정보 조회

### 아키텍처

- **클라이언트**: Java Swing GUI (`loginScreen.java`, `mainScreen.java` 등)
- **서버**: 소켓 기반 서버 (`server.java`, `MainHandler.java`)로 클라이언트 요청 처리
- **데이터베이스**: MySQL (`chatting` 데이터베이스)로 사용자 정보, 친구 목록, 채팅방 정보 저장

### 특징

- RSA 암호화를 통한 비밀번호 보안
- 프로토콜 기반 통신 (`Protocol.java`)으로 클라이언트-서버 간 데이터 교환
- 멀티스레딩 (`MainHandler.java`)을 활용한 동시 사용자 처리

## 주요 파일 및 역할

### 클라이언트 관련

- **`client.java`**  
  - 애플리케이션 진입점
  - `loginScreen`을 실행해 로그인 화면을 표시

- **`loginScreen.java`**  
  - 로그인 및 회원가입 UI 제공
  - 소켓으로 서버와 연결하며, RSA 암호화로 비밀번호를 처리
  - 서버의 프로토콜 메시지를 처리해 화면 전환(로그인 성공 시 `mainScreen`으로 이동).
  - 이메일 인증과 ID 중복 체크 기능 포함

- **`mainScreen.java`**  
  - 로그인 후 메인 화면
  - 친구 목록, 사용자 정보, 오늘의 한마디 표시
  - 친구 추가, 그룹 채팅, 로그아웃 버튼 제공
  - 공공 API로 일일 코로나19 확진자/사망자 정보 표시
- **`chattingScreen.java`**  
  - 채팅방 UI
  - 메시지 입력/출력, 참여자 목록 표시, 방 나가기 기능
  - 프로토콜(`CHATTINGSENDMESSAGE`, `EXITCHATTINGROOM`)로 서버와 통신
- **`newAccount.java`**  
  - 회원가입 UI
  - ID, 비밀번호, 닉네임, 이름, 이메일, 생년월일 입력
  - 이메일 인증 및 ID 중복 체크 버튼 포함
- **`addFriends.java`**  
  - 친구 추가 UI
  - 사용자 검색 및 친구 추가 팝업 메뉴 제공
- **`makeGroupChat.java`**  
  - 그룹 채팅방 생성 UI
  - 친구 목록에서 선택한 사용자들과 채팅방 생성

### 서버 관련

- **`server.java`**  
  - 서버 메인 클래스
  - 포트 9500에서 클라이언트 연결 수락
  - MySQL 데이터베이스(`chatting`)와 JDBC로 연결
  - 각 클라이언트 연결마다 `MainHandler` 스레드 생성
  - 사용자 목록, 접속자 목록, 채팅방 목록 관리
- **`MainHandler.java`**  
  - 클라이언트별 요청 처리 스레드
  - `Protocol.java`의 프로토콜을 파싱해 회원가입, 로그인, 채팅, 친구 관리 요청 처리
  - MySQL 쿼리로 데이터베이스 작업 수행
  - RSA 복호화로 비밀번호 검증
- **`Protocol.java`**  
  - 클라이언트-서버 간 통신 프로토콜 정의(`REGISTER`(회원가입), `ENTERLOGIN`(로그인), `CHATTINGSENDMESSAGE`(메시지 전송))
  - 각 프로토콜은 고유 코드(`100`, `120`)로 구분

### 데이터 객체

- **`User.java`**  
  - 사용자 정보(고유 번호, ID, 비밀번호, 닉네임, 이름, 이메일, 나이, 오늘의 한마디, 접속 상태) 저장
- **`Room.java`**  
  - 채팅방 정보(방 ID, 사용자 수, 방장 이름, 참여자 목록) 저장

### 보안 및 유틸리티

- **`RSA.java`**  
  - RSA 암호화/복호화 구현
  - 공개키(`publickey.txt`)로 비밀번호 암호화, 개인키(`privatekey.txt`)로 복호화
- **`SendMail.java`**  
  - 이메일 인증을 위한 인증번호 전송
  - 네이버 SMTP 서버 사용(현재 `user`/`password` 미설정으로 동작 불가)

## 주요 기능 상세

### 회원가입 및 로그인

- **회원가입** (`Protocol.REGISTER`, `IDSEARCHCHECK`)
    - `newAccount` UI에서 ID, 비밀번호, 닉네임 등 입력
    - 서버에 `IDSEARCHCHECK` 요청으로 ID 중복 확인
    - `SendMail`로 이메일 인증 후 인증번호 확인
    - 비밀번호는 RSA 암호화 후 `UserContent` 테이블에 저장
    - 고유 번호는 `id_generator` 테이블의 `seq_currval`로 생성
- **로그인** (`Protocol.ENTERLOGIN`)
    - `loginScreen`에서 ID와 비밀번호 입력, 비밀번호 RSA 암호화
    - 서버에서 비밀번호 복호화 후 `UserContent` 테이블과 비교
    - 성공 시 사용자 정보와 친구 목록 전송(`ENTERLOGIN_OK`)

### 친구 관리

- **친구 추가** (`Protocol.REQUEST_FRIEND_ADD`)
    - `addFriends` UI에서 사용자 검색 후 추가 요청
    - 서버는 `friendList` 테이블에 친구 정보 삽입
- **친구 삭제** (`Protocol.REQUEST_FRIEND_DELETE`)
    - `mainScreen`에서 친구 선택 후 삭제 요청
    - 서버는 `friendList`에서 해당 레코드 삭제
- **친구 정보 조회** (`Protocol.CHECK_FRIEND_INFO`)
    - 친구 선택 시 서버에서 ID, 닉네임, 이메일 등 조회 후 클라이언트로 전송

### 채팅

- **채팅방 생성** (`Protocol.REQUEST_MAKE_GROUPCHAT`, `ROOMMAKE_OK`)
    - `makeGroupChat`에서 친구 선택 후 채팅방 생성 요청
    - 서버는 `room_number` 테이블에서 새 방 ID 생성, `Room` 객체 추가
- **메시지 전송** (`Protocol.CHATTINGSENDMESSAGE`)
    - `chattingScreen`에서 입력한 메시지를 서버로 전송
    - 서버는 해당 방의 모든 사용자에게 메시지 브로드캐스트(`CHATTINGSENDMESSAGE_OK`)
- **방 입장/퇴장** (`Protocol.JOINROOM_YES`, `EXITCHATTINGROOM`)
    - 초대 수락 시 방 입장, 사용자 목록 업데이트
    - 퇴장 시 방에서 제거, 마지막 사용자라면 방 삭제

### 오늘의 한마디

- **변경** (`Protocol.CHANGE_TODAY_LINE`)
    - `mainScreen`에서 입력한 문자열을 서버로 전송
    - 서버는 `UserContent`와 `friendList` 테이블의 `today_line` 업데이트
- **업데이트 알림** (`Protocol.UPDATED`, `UPDATE_PLZ`)
    - 변경 시 모든 접속자에게 업데이트 알림 전송
    - 클라이언트는 최신 친구 목록 요청(`UPDATE_CONFIRM`).

### 코로나19 정보

- `mainScreen`에서 공공 API(`http://openapi.data.go.kr`) 호출로 일일 확진자/사망자 표시
- XML 파싱(XPath 사용)으로 데이터 추출

## 기술 스택

- **언어**: Java
- **프론트엔드**: Java Swing (GUI)
- **백엔드**:
    - 소켓 프로그래밍 (`ServerSocket`, `Socket`)
    - 멀티스레딩 (`Thread`, `MainHandler`)
    - JDBC (MySQL 연결)
- **데이터베이스**: MySQL (`chatting` 데이터베이스)
    - 테이블: `UserContent`, `friendList`, `id_generator`, `room_number`
- **보안**: RSA 암호화 (비밀번호)
- **외부 API**:
    - 네이버 SMTP (이메일 인증)
    - 공공 데이터 포털 (코로나19 정보)
- **파일 처리**: `publickey.txt` (공개키), `privatekey.txt` (개인키), `serverinfo.dat` (서버 IP/포트)

## 데이터베이스 구조

- **`UserContent`**
    - 사용자 정보 저장
    - 컬럼: `priNumber` (고유 번호), `id`, `password` (RSA 암호화), `nickName`, `name`, `email`, `age`, `today_line`, `state` (0: 오프라인, 1: 온라인)
- **`friendList`**
    - 친구 관계 저장
    - 컬럼: `id` (사용자 ID), `friendId`, `friendName`, `friendNickname`, `friendtoday_line`, `friendState`
- **`id_generator`**
    - 사용자 고유 번호 생성 (`seq_currval`)
- **`room_number`**
    - 채팅방 ID 생성 (`room_num`)