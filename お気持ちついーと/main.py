import tweepy
import datetime
import requests
import urllib.parse

# python で Twitter APIを使用するためのConsumerキー、アクセストークン設定
Consumer_key = ''
Consumer_secret = ''
Access_token = ''
Access_secret = ''

# 認証
auth = tweepy.OAuthHandler(Consumer_key, Consumer_secret)
auth.set_access_token(Access_token, Access_secret)
api = tweepy.API(auth, wait_on_rate_limit=True)

# 今日と昨日の時刻を取得
today_datetime = datetime.date.today()
yesterday_datetime = (today_datetime-datetime.timedelta(days=1))
# today_datetime=datetime.date.today()
# yesterday_datetime = (today_datetime-datetime.timedelta(days=3))
# today_datetime=(today_datetime-datetime.timedelta(days=2))

# 取得した時刻に応じたツイートを取得して感情分析を実施
tweet_num = 0
likedislike = []
joysad = []
angerfear = []
for tweet in tweepy.Cursor(api.search, q="from:10kaoru12 source:twitter_for_iphone exclude:replies exclude:retweets", since=yesterday_datetime, until=today_datetime).items():
    tweet_num += 1
    response = requests.get(
        "http://ap.mextractr.net/ma9/emotion_analyzer?&out=json&apikey=&text="+urllib.parse.quote(tweet.text)+"\"").json()
    likedislike.append(float(response.get("likedislike")))
    joysad.append(float(response.get("joysad")))
    angerfear.append(float(response.get("angerfear")))
    print(tweet.text)
    print(likedislike, joysad, angerfear)

# 感情分析結果の平均値を計算して絵文字を決める
total_sentiment = []
total_sentiment = sum(likedislike)/tweet_num, sum(joysad) / \
    tweet_num, sum(angerfear)/tweet_num
print(total_sentiment)
emoji = ""
if(total_sentiment[0] > 0.5):
    emoji = "😍"
elif(total_sentiment[0] < -0.5):
    emoji = "😒"
elif(total_sentiment[1] > 0.5):
    emoji = "😆"
elif(total_sentiment[1] < -0.5):
    emoji = "😢"
elif(total_sentiment[2] > 0.5):
    emoji = "😠"
elif(total_sentiment[2] < -0.5):
    emoji = "😨"
else:
    emoji = "🙂"

# twitterの名前を変更
api.update_profile(name="ざわお" + emoji)
