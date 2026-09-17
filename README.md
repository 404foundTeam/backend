# 🐝 MarketBEE  
소상공인을 위한 AI 기반 카드뉴스 생성 & 스마트 리포트 플랫폼  
<br>
> **배포 주소**: [http://marketbee.site](http://marketbee.site)

> **시연 영상**: [http://www.youtube.com/watch?v=qiTL4bHeLFo&t=4s](https://www.youtube.com/watch?v=qiTL4bHeLFo&t=4s)

<br>

## 👥 팀원 구성 및 역할

| 이름   | 직무         | 역할 |
|--------|-------------|------|
| 🙇‍♂️ 우승연 | 팀장 / 백엔드 | - 웰컴페이지, 마이페이지, 카드뉴스 API 개발 <br> - RDS 설계 및 Redis 기반 무료 이용 횟수 관리 <br> - AWS EC2 + Nginx 배포 및 GitHub Actions CI/CD 구축 |
| 조은진 | 백엔드       | - 스마트리포트, 맞춤형 마케팅, 지도 이용 제휴 API 개발 <br> - 매출/리뷰 데이터 파싱·크롤링 및 분석 파이프라인 구축 |
| 유재원 | 백엔드       | - 사진 촬영 가이드 API 개발 <br> - 소상공인 인터뷰 및 사용자 요구사항 조사 |
| 한종민 | 프론트엔드   | - React 기반 웰컴페이지/카드뉴스/사진촬영가이드/지도제휴 개발 <br> - Axios 연동 및 상태 관리 <br> - 프론트엔드 아키텍처 전반 구현 |
| 신상민 | 프론트엔드   | - React 기반 마이페이지/스마트리포트/마케팅분석 개발 <br> - 캘린더 뷰 및 API 연동 |
| 이채영 | 디자인       | - 서비스 UI/UX 디자인 <br> - 피그마 와이어프레임 제작 |


<br>

## 📖 프로젝트 소개
MarketBEE는 소상공인의 **폐업률 감소와 매출 증대**를 목표로 하는 AI 플랫폼입니다.  
홍보와 운영 전략에서 어려움을 겪는 자영업자들을 위해, **AI 카드뉴스 생성**, **스마트 리포트**, **맞춤형 마케팅 제안** 기능을 제공하여 사장님들이 **본업에 더 집중**할 수 있도록 돕습니다.  
<br>
<br>

## ✨ 주요 기능

- **AI 카드뉴스 생성**  
  - GPT가 홍보 문구 자동 생성 + 이미지 API로 배경 이미지 생성  
  - 프론트에서 텍스트 합성 → AWS S3에 저장 & 마이페이지에서 열람 가능  

- **AI 스마트 리포트**  
  - 카카오맵 리뷰 분석 (핵심 키워드, 평균 평점)  
  - 매출 데이터 기반 시각화 (매출 추이, 메뉴별 판매 순위 등)  
  - 리뷰+매출 데이터를 종합해 **실질적 개선 팁 제공**  

- **맞춤형 마케팅 제안**  
  - 매출 데이터를 기반으로 매장의 강·약점 분석  
  - 즉시 실행 가능한 **마케팅 이벤트** 자동 제안  

- **제휴 관리 (B2B 기능)**  
  - 카카오맵 기반 주변 업장과 제휴 관계 맺기 및 관리  

- **사진 촬영 가이드**  
  - 업로드한 사진을 AI가 분석해 구도·조명·채도 개선 팁 제공  
<br>
<br>

## ⚙️ 기술 스택

**Backend**  
- Java 21, Spring Boot  
- MariaDB (AWS RDS), Redis  
- AWS S3, AWS EC2
- Python (크롤링, venv)  

**Frontend**  
- npm, React, JavaScript, HTML  
- Module CSS
- React Router, Zustand, Axios  
- React Calendar, React Canvas, Chart.js, React Toastify, React Bubble Chart d3

**Infa/CI-CD**
- GitHub Actions, nginx, EC2

**Tools**
- VSC, Intellij IDEA, GitHub, Figma, Notion

**외부 연동**
- OpenAI GPT API, Naver Clova OCR, 국세청 사업자등록 진위확인 API, 카카오맵 API, 공공데이터포털(공휴일) API

<br>
<br>

## ⚒️ 서비스 아키텍쳐
<img width="4262" height="2590" alt="image" src="https://github.com/user-attachments/assets/72fdb6d1-3684-4f06-a921-91b83cfdb5ba" />

<br>
<br>
