# 🚀 Spring Boot 기반 커뮤니티 게시판 프로젝트
> **Spring Boot와 JPA를 활용한 CRUD 및 보안 강화 커뮤니티 웹 서비스**
>
> <img width="1878" height="965" alt="2026-04-06 17_03_43-demo – BoardService java  demo main" src="https://github.com/user-attachments/assets/8ae64bb2-126a-43d3-abc2-ecf9c747c0d8" />

목차

# 1. 프로젝트 개요
- **개발 기간**: 2026.02 ~ 2026.03
- **주요 목적**: CRUD 게시판을 바탕으로 Spring Security를 통한 인증 보안 강화 및 JPA 기반 동적 검색 기능 등 구현 연습
## 프로젝트 기능
- **게시판** - 권한 별 CRUD 기능, 조회수, 페이징 및 검색 처리
- **사용자** - Security를 통한 회원가입 및 로그인, OAuth 2.0 네이버, 구글 로그인, 회원 정보 수정, 유효성 검사 및 중복 검사, 이메일 인증번호 발송, 소셜 계정 첫 로그인 시 닉네임 설정
- **댓글** - 권한 별 CRUD 기능
## 2. **핵심 기술**
### 2-1. 백엔드
**주요 프레임워크 / 라이브러리**
- Java 21
- SpringBoot 3.5.10
- JPA(Spring Data JPA)
- Spring security 6
- OAuth 2.0
- Spring Boot Starter Validation
- Lombok

**Build Tool**
- Gradle 8.14.3

**DataBase**
- MySQL 8.0.45

### 2-2. 프론트엔드
- Html
- JavaScript
- Bootstrap 5.3.2
- Thymeleaf
- AJAX
# 2. 실행 화면
<details>
<summary>🔍 게시글 관련 보기 (클릭)</summary>

## **1. 게시글 목록**

   <img width="1280" height="713" alt="image" src="https://github.com/user-attachments/assets/84f5f09c-1680-48cf-9894-4a983036f3c2" />
   
전체 목록을 페이징 처리하여 조회가 가능하다.

## **2. 게시글 등록**

<img width="1021" height="1001" alt="image" src="https://github.com/user-attachments/assets/271a4923-7a92-4c21-a4fd-6bfa0ddc37c2" />

비회원은 2~10자 ID, 수정,삭제에 사용할 비밀번호(최소4자리)를 입력해야 게시글 등록이 가능하다. 작성 후 목록 화면으로 redirect

<img width="1001" height="838" alt="image" src="https://github.com/user-attachments/assets/7fa5850a-b865-432e-a821-dc56b7667145" />

회원은 세션에 입력된 이메일 정보로 인해 제목과 내용을 자유롭게 작성할 수 있다. 작성 후 목록 화면으로 redirect

## **3. 게시글 상세보기**

<img width="1280" height="840" alt="image" src="https://github.com/user-attachments/assets/0e3fbda5-1b06-47b7-a396-8de0577c6442" />

비회원 세션

<img width="1280" height="824" alt="image" src="https://github.com/user-attachments/assets/56a27196-6956-439c-825c-e79d5cb01797" />

회원은 오른쪽 상단에 닉네임이 보인다.

## **4. 게시글 수정화면**

<img width="972" height="994" alt="image" src="https://github.com/user-attachments/assets/66748a1f-765f-471a-9068-e2852115575f" />

<img width="1107" height="842" alt="image" src="https://github.com/user-attachments/assets/2b223b6b-7a68-4b14-a434-9e40cf49497b" />

<img width="1280" height="814" alt="image" src="https://github.com/user-attachments/assets/7a0c4f70-f421-441b-ae23-45399a55be2a" />

비회원은 등록할 시 입력했던 비밀번호를 입력해야 수정이 가능하다.

<img width="1280" height="827" alt="image" src="https://github.com/user-attachments/assets/b7b99cb2-49a6-44db-99b5-bfd24ce76bd5" />

<img width="1064" height="728" alt="image" src="https://github.com/user-attachments/assets/9de3b5d7-365c-4897-9c8e-f6c67470b596" />

<img width="1280" height="810" alt="image" src="https://github.com/user-attachments/assets/2876b5c4-fbfe-4e1e-a91e-43410355f71a" />

회원은 본인의 글을 자유롭게 수정할 수 있다. 수정하고 나면 게시글 상세보기 화면으로 redirect한다.

## **5. 게시글 삭제화면**

<img width="1280" height="832" alt="image" src="https://github.com/user-attachments/assets/919de28d-fc9d-4de1-a45a-9768822e4126" />
<img width="717" height="634" alt="image" src="https://github.com/user-attachments/assets/d89e4e9a-e1a7-45ca-85ac-4f44414f2e48" />

비회원은 비밀번호 입력창에 작성에 사용했던 비밀번호를 입력해야 삭제가 가능하다. 삭제 후 목록 리스트로 redirect

<img width="1280" height="763" alt="image" src="https://github.com/user-attachments/assets/a563ccf0-fc4e-480b-8e37-eada6d6365a9" />

회원은 본인의 글을 자유롭게 삭제할 수 있다. 삭제 후 목록 리스트로 redirect

## **6. 게시글 검색화면**

<img width="1280" height="596" alt="image" src="https://github.com/user-attachments/assets/11326f83-b638-4ce5-aa99-ccb82c2c905e" />

검색 키워드를 입력하고 검색을 누르면 키워드가 포함된 게시글이 나타난다. 옵션은 1. 제목+내용 2. 제목 3. 내용 4. 작성자

<img width="1280" height="814" alt="image" src="https://github.com/user-attachments/assets/a66c3b2b-06eb-48ce-bb1c-c3a967c0d136" />
<img width="1280" height="456" alt="image" src="https://github.com/user-attachments/assets/c1058213-a92c-4e67-bcdb-4dbe4f78e37b" />

</details>

<details>
<summary>🔍 회원 관련 보기 (클릭)</summary>
   
## **1. 회원가입 화면**

<img width="625" height="816" alt="2026-04-06 22_01_11-demo – save html  demo main" src="https://github.com/user-attachments/assets/acbd76d6-3d34-48f4-8a39-6e23e18f41bd" />
<img width="576" height="975" alt="2026-04-06 22_07_51-demo – save html  demo main" src="https://github.com/user-attachments/assets/4467f820-c5c1-4328-a09b-f99c86efa509" />

유효성 검사 및 중복확인를 통과해야 하며 메일을 수신할 수 있는 이메일을 입력해야한다.

<img width="591" height="1042" alt="image" src="https://github.com/user-attachments/assets/fd44636f-7c04-42d3-9ecd-1245e5ff1c28" />

DB에 있는 이메일을 입력하면 중복으로 인해 불가능한 모습

<img width="596" height="952" alt="2026-04-06 22_02_32-demo – save html  demo main" src="https://github.com/user-attachments/assets/f4a3d697-b299-47eb-9008-a7dcd2de50ae" />
<img width="596" height="899" alt="2026-04-06 22_02_42-demo – save html  demo main" src="https://github.com/user-attachments/assets/72fcd401-779b-4822-9560-0c6ea9d8658c" />
<img width="582" height="880" alt="2026-03-23 20_52_27-Inbox (9) - teraafteryou5@gmail com - Gmail — Mozilla Firefox" src="https://github.com/user-attachments/assets/a60dd9ea-fc05-4000-9eb2-6238a77abc1b" />

인증 시간 3분이 지나면 재발송 버튼을 눌러서 새로운 인증번호를 입력해야한다.

## **2. 로그인 화면**

<img width="549" height="792" alt="image" src="https://github.com/user-attachments/assets/60fab1af-6d45-4b1a-9565-e93d8500e9f5" />
<img width="592" height="931" alt="image" src="https://github.com/user-attachments/assets/f6013bb3-1cc7-400f-b46a-7516ce1eeaf4" />

아이디 또는 비밀번호 일치하지 않을 때 로그인을 시도한 모습

### **2.1 OAuth 2.0 소셜 로그인 화면
<img width="1280" height="632" alt="image" src="https://github.com/user-attachments/assets/71478aa7-3702-414c-a28a-42190adc2fe0" />

구글 로그인

<img width="960" height="783" alt="image" src="https://github.com/user-attachments/assets/969e6420-fccf-4158-b226-7ec439c9d6b8" />

네이버 로그인

## **3. 회원정보 수정**
이메일(readonly), 이름(소셜 계정 전용), 닉네임과 비밀번호 변경 가능, 완료하면 게시판 목록으로 redirect

<img width="623" height="903" alt="2026-04-07 19_27_16-demo – memberUpdate html  demo main" src="https://github.com/user-attachments/assets/b2960692-f8fb-42a6-beeb-34a1db2db781" />

유효규칙을 지키지 않으면 수정 완료 버튼이 활성화되지않는다. 

<img width="585" height="810" alt="2026-04-07 19_28_54-demo – memberUpdate html  demo main" src="https://github.com/user-attachments/assets/ad4b0dca-67f4-482d-b15e-3ce94b18ecfb" />

비밀번호를 변경하지 않고 닉네임만 변경할 수 있다.

</details>

<details>
<summary>🔍 댓글 관련 보기 (클릭)</summary>
   
## **1. 댓글 작성 화면**

<img width="1460" height="894" alt="2026-04-07 19_30_44-demo – memberUpdate html  demo main" src="https://github.com/user-attachments/assets/fcfee7af-bd6b-4998-9eeb-ab8ed75eaef4" />

비회원으로 댓글을 등록하는 모습, ID(2~10자), 최소4자리 비밀번호를 입력해야 등록이 가능하다.

<img width="1539" height="981" alt="2026-04-07 19_31_12-demo – memberUpdate html  demo main" src="https://github.com/user-attachments/assets/159fbec7-ee65-4d62-98b2-23853c389076" />

회원으로 등록하는 모습, 댓글 내용만 입력하면 된다. AJAX 비동기로 인하여 재로딩없이 등록

## **2. 댓글 수정 화면**

<img width="1280" height="1005" alt="image" src="https://github.com/user-attachments/assets/9b80be79-8997-4ce3-8d12-b0e695c79e86" />

비회원은 등록할 때 사용한 비밀번호를 입력해야 수정이 가능하다.

<img width="1280" height="962" alt="image" src="https://github.com/user-attachments/assets/3d9acd51-892f-4a32-ad5a-295a4115727b" />

<img width="1280" height="877" alt="image" src="https://github.com/user-attachments/assets/404d8d75-a59f-4fb5-baf0-22370b836151" />

본인이 등록한 댓글만 수정이 가능하다. AJAX 비동기로 인하여 재로딩없이 댓글 수정

## **3. 댓글 삭제 화면**

<img width="1280" height="1009" alt="image" src="https://github.com/user-attachments/assets/4058acbb-6daf-4a0b-a9b7-e56f791fbf5a" />

<img width="1280" height="1032" alt="image" src="https://github.com/user-attachments/assets/ca9e7c23-c8bb-434a-991e-c5060182f6dd" />

비회원 댓글, 삭제도 똑같이 비밀번호를 입력해야 삭제가 가능하다.

<img width="1280" height="944" alt="image" src="https://github.com/user-attachments/assets/ca4dcaf8-e18e-4adf-8051-fbedd5bf1673" />

<img width="1280" height="754" alt="image" src="https://github.com/user-attachments/assets/9eddcf37-e7be-45c9-9a37-4bd882deafd7" />

회원 댓글, 본인이 직접 작성한 댓글을 삭제할 수 있다. AJAX 비동기로 인하여 재로딩없이 댓글 삭제

</details>

# **3. 구조 및 설계**
## **1. 패키지 구조**
<details>
<summary> 패키지 구조 (클릭)</summary>

### 📂 Project Structure

```text
src
 ├─main
 │  ├─generated
 │  ├─java
 │  │  └─com
 │  │      └─example
 │  │          └─demo
 │  │              │  DemoApplication.java
 │  │              │  
 │  │              ├─config      <-- 설정 관련
 │  │              │      CustomOAuth2User.java
 │  │              │      LoginCheckInterceptor.java
 │  │              │      PasswordConfig.java
 │  │              │      SecurityConfig.java
 │  │              │      
 │  │              ├─controller  <-- 컨트롤러 계층
 │  │              │      BoardController.java
 │  │              │      CommentController.java
 │  │              │      HomeController.java
 │  │              │      MemberController.java
 │  │              │      
 │  │              ├─domain
 │  │              │      Role.java
 │  │              │      
 │  │              ├─dto         <-- 데이터 전송 객체
 │  │              │      BoardDTO.java
 │  │              │      CommentDTO.java
 │  │              │      MemberDTO.java
 │  │              │      MemberUpdateDTO.java
 │  │              │      OAuthAttributes.java
 │  │              │      
 │  │              ├─entity      <-- DB 엔티티
 │  │              │      BaseTimeEntity.java
 │  │              │      BoardEntity.java
 │  │              │      CommentEntity.java
 │  │              │      MemberEntity.java
 │  │              │      
 │  │              ├─repository  <-- DB 접근 객체
 │  │              │      BoardRepository.java
 │  │              │      CommentRepository.java
 │  │              │      MemberRepository.java
 │  │              │      
 │  │              └─service     <-- 비즈니스 로직
 │  │                      BoardService.java
 │  │                      CommentService.java
 │  │                      CustomOAuth2UserService.java
 │  │                      MailService.java
 │  │                      MemberService.java
 │  │                      
 │  └─resources
 │      │  application-local.yml
 │      │  application-oauth.yml
 │      │  application.yml
 │      │  
 │      ├─static      <-- 정적 파일 (CSS, JS)
 │      └─templates   <-- 뷰 파일 (HTML)
 │              delete-check.html
 │              detail.html
 │              index.html
 │              join.html
 │              list.html
 │              login.html
 │              memberUpdate.html
 │              paging.html
 │              save.html
 │              set-nickname.html
 │              update.html
```
</details>

## **2. DB 설계**

<img width="894" height="694" alt="image" src="https://github.com/user-attachments/assets/b64e5058-1ccb-4deb-92b3-22a46515198f" />

<img width="730" height="562" alt="image" src="https://github.com/user-attachments/assets/8e2162a8-d3cc-43b9-8a52-a6959aa4fecf" />

<img width="649" height="505" alt="image" src="https://github.com/user-attachments/assets/ca3f8278-17ce-4ad0-806f-990b03eff53c" />

<img width="704" height="483" alt="image" src="https://github.com/user-attachments/assets/55c7eba8-0f9b-415b-ae4f-824cfa57f041" />

## **3. API 설계**

<img width="991" height="623" alt="image" src="https://github.com/user-attachments/assets/a8c7c141-e663-4172-be8a-05c907bdd5a3" />

<img width="913" height="641" alt="image" src="https://github.com/user-attachments/assets/08934e32-68ae-472c-833b-cd5c5ae17373" />

<img width="669" height="275" alt="image" src="https://github.com/user-attachments/assets/f093d7e4-2d00-4577-9c52-afb27933506c" />

## **4. 아키텍처 설계**

<img width="656" height="731" alt="게시판 시스템 아키텍처 drawio" src="https://github.com/user-attachments/assets/08356972-f0fc-464d-90fd-6482b585c375" />

# **개발 내용**

<a href="https://dev-afteryou.tistory.com/2">
  <img src="https://img.shields.io/badge/Tistory-Spring Boot JPA 기반 게시글 조회수 및 비즈니스 로직 구현-orange?style=for-the-badge&logo=tistory">
</a><br>
<a href="https://dev-afteryou.tistory.com/3">
  <img src="https://img.shields.io/badge/Tistory-Spring Boot Data JPA Pageable을 활용한 대용량 데이터 페이징-orange?style=for-the-badge&logo=tistory">
</a><br>
<a href="https://dev-afteryou.tistory.com/4">
  <img src="https://img.shields.io/badge/Tistory-Spring Data JPA Method Query를 활용한 조건별 동적 검색 및 페이징 구현-orange?style=for-the-badge&logo=tistory">
</a><br>
<a href="https://dev-afteryou.tistory.com/5">
  <img src="https://img.shields.io/badge/Tistory-Spring Security와 OAuth 2.0 기반 통합 인증 시스템 구축(회원가입, 로그인)-orange?style=for-the-badge&logo=tistory">
</a><br>
<a href="https://dev-afteryou.tistory.com/6">
  <img src="https://img.shields.io/badge/Tistory-게시판 인증 아키텍처 리팩토링 및 트러블슈팅을 통한 보안 강화-orange?style=for-the-badge&logo=tistory">
</a><br>
<a href="https://dev-afteryou.tistory.com/7">
  <img src="https://img.shields.io/badge/Tistory-회원가입 시 JavaMailSender를 활용한 비대면 본인 인증 프로세스 구현-orange?style=for-the-badge&logo=tistory">
</a><br>
<a href="https://dev-afteryou.tistory.com/8">
  <img src="https://img.shields.io/badge/Tistory-SMTP 보안 강화 및 세션 관리 전략(Timeout, 재발송) 최적화-orange?style=for-the-badge&logo=tistory">
</a><br>
<a href="https://dev-afteryou.tistory.com/9">
  <img src="https://img.shields.io/badge/Tistory-Spring Security를 활용한 CSRF 공격 방어: Thymeleaf와 AJAX 적용 사례-orange?style=for-the-badge&logo=tistory">
</a><br>
<a href="https://dev-afteryou.tistory.com/10">
  <img src="https://img.shields.io/badge/Tistory-Spring Boot 게시판 Authentication 객체 동기화를 통한 회원 정보 수정 처리-orange?style=for-the-badge&logo=tistory">
</a><br>
<a href="https://dev-afteryou.tistory.com/11">
  <img src="https://img.shields.io/badge/Tistory-CustomAuthenticationFailureHandler를 통한 예외 처리 커스텀(로그인 실패)-orange?style=for-the-badge&logo=tistory">
</a><br>
<a href="https://dev-afteryou.tistory.com/12">
  <img src="https://img.shields.io/badge/Tistory-OAuth 2.0 기반 소셜 로그인(Google, Naver) 연동 및 확장-orange?style=for-the-badge&logo=tistory">
</a><br>
<a href="https://dev-afteryou.tistory.com/13">
  <img src="https://img.shields.io/badge/Tistory-세션 기반 게시글 권한 제어 및 인가(Authorization) 로직-orange?style=for-the-badge&logo=tistory">
</a><br>
<a href="https://dev-afteryou.tistory.com/14">
  <img src="https://img.shields.io/badge/Tistory-인가 로직 리팩토링: 비정상적 접근 차단 및 예외 처리 프로세스-orange?style=for-the-badge&logo=tistory">
</a><br>
<a href="https://dev-afteryou.tistory.com/15">
  <img src="https://img.shields.io/badge/Tistory-Spring Boot AJAX를 활용한 비동기 댓글 등록 및 실시간 UI 렌더링-orange?style=for-the-badge&logo=tistory">
</a><br>
<a href="https://dev-afteryou.tistory.com/16">
  <img src="https://img.shields.io/badge/Tistory-RESTful API 기반 댓글 CRUD API 설계 및 구현-orange?style=for-the-badge&logo=tistory">
</a><br>
<a href="https://dev-afteryou.tistory.com/17">
  <img src="https://img.shields.io/badge/Tistory-조건부 렌더링을 활용한 댓글 관리 권한 분리 및 UI UX 개선-orange?style=for-the-badge&logo=tistory">
</a><br>
<a href="https://dev-afteryou.tistory.com/18">
  <img src="https://img.shields.io/badge/Tistory-Spring Validation을 활용한 비인증 사용자 입력값 검증-orange?style=for-the-badge&logo=tistory">
</a><br>
<a href="https://dev-afteryou.tistory.com/19">
  <img src="https://img.shields.io/badge/Tistory-트러블슈팅: 타임리프 폼 바인딩 오류 및 페이지 튕김 현상-orange?style=for-the-badge&logo=tistory">
</a><br>
<a href="https://dev-afteryou.tistory.com/20">
  <img src="https://img.shields.io/badge/Tistory-RedirectAttributes를 활용한 PRG 패턴 구현 및 데이터 유지-orange?style=for-the-badge&logo=tistory">
</a><br>
<a href="https://dev-afteryou.tistory.com/22">
  <img src="https://img.shields.io/badge/Tistory-set--nickname 소셜 전용 닉네임 수정 리팩토링-orange?style=for-the-badge&logo=tistory">
</a><br>

# 프로젝트 보완할 점

기본적인 CRUD게시판을 공부한 뒤 제가 구상하고 싶은 기능들을 하나씩 추가하면서 진행했습니다.

처음에는 아무런 권한없이 글만 쓰는 게시판이었으나 점차 제가 원하는 기능들인 회원가입, 로그인, 소셜 로그인, SMTP를 이용한 메일발송, 회원정보 수정, 댓글 등록, 수정, 삭제, 페이징 등을 추가했습니다.

타임리프는 '내추럴 템플릿'으로서 서버 없이도 퍼블리싱 결과를 바로 확인할 수 있어 생산성 면에서 유리하다고 생각합니다. 또한 스프링 부트와의 강력한 통합기능 덕분에 유효성 검사나 시큐리티 적용을 더 정교하게 구현할수 있어 좋았습니다.

AJAX, Security를 추가하면서 다양한 에러가 발생하면서 기존에 구상해둔 기능들을 전부 구현하지 못했습니다.

최대한 UX를 신경쓰면서 개발하고 수정해왔지만 댓글을 작성한 시간의 직관성을 높인다거나 댓글 수정을 누르면 나오는 프롬프트없이 바로 수정하게 하는 그런 부분이 아쉽다고 생각합니다.
<details>
<summary> 추가할 기능</summary>

-게시판 조회수 24시간 이내 1번으로 유효

-파일 업로드 기능

-비회원은 자동등록번호 생성 후 입력해야 글 입력 가능

-조회수순으로 정렬

-내비게이션 바 추가

-댓글 페이징 기능

</details>

# 후기
CRUD게시판을 우선 공부하고 나머지 기능을 구현하기위해 요구사항 리스트를 작성했었습니다.처음에는 기본적인 기능들만 구현하려고 했으나 기왕할거면 마무리까지 잘하고 싶어 제가 생각하는 로직의 구현에 힘썼습니다.

기능들을 하나씩 추가하고 에러가 발생하고 발생할 때 마다 트러블슈팅를 작성하여 다음에는 같은 실수를 방지하려고 했습니다. 또한 미흡한 부분이 있다면 리팩토링도 많이하여 UX와 UI의 균형을 이루고자 노력했습니다.

그런 과정끝에 제가 모르는 부분들을 알게 됐고 더 나은 코드가 무엇인지 고민할 수 있었습니다.

BoardDTO와 BoardEntity(DB구조)의 변환, 비동기 방식을 위한 @ResponseBody와 Ajax의 관계, 처음 소셜 로그인을 할 때 닉네임을 설정하지 않아 이름이 대신 나올 수도 있기때문에 닉네임을 먼저 설정하는 선행 로직의 필요성이라든가
이메일 인증번호가 개발자도구로 인해 확인이 가능해서 추가적인 보안 프로세스 등 자세한 로직이 중요하다는 경험을 했습니다.

이번 프로젝트를 해오면서 사용자의 요청을 받아 서비스로 전달하는 Controller, 비즈니스 로직을 처리하고 리포지토리를 호출하는 Service, 실제 DB에 접근해 필요한 데이터를 가져오는 Repositry, 가져온 Entity를 안전한 DTO로 변환하여
컨트롤러에 돌려주는 Service, 가공된 데이터를 모델에 담아 사용자에게 HTML로 보여주는 Controller의 흐름을 이해하게 됐고 다른 프로젝트를 진행할 때 도움이 될 것이라고 생각합니다.

끝까지 읽어주셔서 감사합니다.
