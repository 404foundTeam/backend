import os
import time
import hashlib
import sys
from datetime import datetime
from dateutil.relativedelta import relativedelta
import pymysql
from bs4 import BeautifulSoup
from selenium import webdriver
from selenium.common.exceptions import TimeoutException
from selenium.webdriver.common.by import By
from selenium.webdriver.support.ui import WebDriverWait
from selenium.webdriver.support import expected_conditions as EC
from webdriver_manager.chrome import ChromeDriverManager
from selenium.webdriver.common.keys import Keys
from selenium.webdriver.chrome.service import Service
from selenium.webdriver.chrome.options import Options

DB_CONFIG = {
    'host': os.getenv('DB_HOST', 'localhost'),
    'user': os.getenv('DB_USER', 'root'),
    'password': os.getenv('DB_PASSWORD', ''),
    'db': os.getenv('DB_NAME', 'marketbee'),
    'charset': 'utf8mb4',
    'cursorclass': pymysql.cursors.DictCursor
}

def get_db_connection():
    try:
        conn = pymysql.connect(**DB_CONFIG)
        return conn
    except pymysql.MySQLError as e:
        print(f"데이터베이스 연결 오류: {e}")
        return None

def insert_review(conn, place_name, review_date, rating, content):
    review_hash = hashlib.sha256(content.encode('utf-8')).hexdigest()
    with conn.cursor() as cursor:
        sql = "INSERT IGNORE INTO reviews (place_name, review_date, rating, content, review_hash) VALUES (%s, %s, %s, %s, %s)"
        cursor.execute(sql, (place_name, review_date, rating, content, review_hash))
    conn.commit()

def get_kakao_review_link(place_name):
    options = webdriver.ChromeOptions()
    options.add_argument("--headless")
    driver = webdriver.Chrome(options=options)
    driver.get("https://map.kakao.com/")

    try:
        search_box = WebDriverWait(driver, 10).until(
            EC.presence_of_element_located((By.ID, "search.keyword.query"))
        )
        search_box.clear()
        search_box.send_keys(place_name)
        search_box.send_keys(Keys.ENTER)

        WebDriverWait(driver, 10).until(
            EC.presence_of_element_located((By.ID, "info.search.place.list"))
        )

        review_link_element = WebDriverWait(driver, 10).until(
            EC.presence_of_element_located(
                (By.CSS_SELECTOR, r"#info\.search\.place\.list > li:first-child a.numberofscore")
            )
        )

        review_url = review_link_element.get_attribute('href')
        return review_url

    except Exception as e:
        print(f"오류 발생: {e}")
        return None
    finally:
        driver.quit()

def crawl_kakao_reviews(place_url):
    today = datetime.today()
    start_date = (today.replace(day=1) - relativedelta(months=6)).strftime("%Y-%m-%d")
    end_date = (today.replace(day=1) - relativedelta(days=1)).strftime("%Y-%m-%d")
    print(f"크롤링 대상 기간: {start_date} ~ {end_date}")

    chrome_options = Options()
    chrome_options.add_argument("--headless")
    chrome_options.add_argument("--no-sandbox")
    chrome_options.add_argument("--disable-dev-shm-usage")
    chrome_options.add_argument("user-agent=Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/98.0.4758.102 Safari/537.36")
    chrome_options.add_experimental_option("excludeSwitches", ["enable-automation"])
    chrome_options.add_experimental_option('useAutomationExtension', False)

    service = Service(ChromeDriverManager().install())
    driver = webdriver.Chrome(service=service, options=chrome_options)
    driver.execute_cdp_cmd('Page.addScriptToEvaluateOnNewDocument', {
        'source': "Object.defineProperty(navigator, 'webdriver', {get: () => undefined})"
    })

    db_conn = get_db_connection()
    if not db_conn:
        driver.quit()
        return

    try:
        driver.get(place_url)
        WebDriverWait(driver, 30).until(
            EC.presence_of_element_located((By.CSS_SELECTOR, ".list_review"))
        )

        last_height = driver.execute_script("return document.body.scrollHeight")
        while True:
            driver.execute_script("window.scrollTo(0, document.body.scrollHeight);")
            time.sleep(2)
            new_height = driver.execute_script("return document.body.scrollHeight")
            if new_height == last_height:
                print("모든 리뷰를 로드했습니다.")
                break
            last_height = new_height

        try:
            more_buttons = driver.find_elements(By.CSS_SELECTOR, ".desc_review .btn_more")
            for button in more_buttons:
                try:
                    driver.execute_script("arguments[0].click();", button)
                    time.sleep(0.2)
                except Exception:
                    pass
        except Exception as e:
            print(f"오류 발생: {e}")

        html = driver.page_source
        soup = BeautifulSoup(html, "html.parser")
        place_name = soup.select_one("h2.tit_head").text
        review_list = soup.select(".list_review > li")
        if not review_list:
            print("리뷰 데이터를 찾지 못했습니다. 페이지 구조를 다시 확인해주세요.")

        for review in review_list:
            review_date_str = review.select_one(".txt_date").text
            review_date = datetime.strptime(review_date_str, '%Y.%m.%d.').strftime("%Y-%m-%d")

            if start_date <= review_date <= end_date:
                rating_element = review.select_one(".starred_grade .screen_out:nth-of-type(2)")
                rating = float(rating_element.text) if rating_element else 0.0

                content = ""
                comment_area = review.select_one(".desc_review")
                if comment_area:
                    more_button = comment_area.select_one(".btn_more")
                    if more_button:
                        more_button.extract()

                    content = comment_area.text.strip()

                print(f"수집된 리뷰: {review_date}, 평점: {rating}, 내용: {content[:30]}...")
                insert_review(db_conn, place_name, review_date, rating, content)

    except TimeoutException:
        print("페이지 로딩 시간 초과. URL이나 네트워크, CSS 선택자(.list_review)를 확인해주세요.")
    except Exception as e:
        print(f"크롤링 중 오류 발생: {e}")
    finally:
        if db_conn:
            db_conn.close()
        driver.quit()
        print("크롤링이 완료되었습니다.")


if __name__ == '__main__':
    if len(sys.argv) > 1:
        place_name = sys.argv[1]
        print(f"전달받은 가게 이름: {place_name}")

        target_url = get_kakao_review_link(place_name)
        if target_url:
            print("리뷰 탭 링크:", target_url)
            crawl_kakao_reviews(target_url)
        else:
            print(f"'{place_name}'의 리뷰 링크를 찾지 못했습니다.")
    else:
        print("에러: 분석할 가게 이름을 인자로 전달해주세요.")