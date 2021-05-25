from flask import Flask,render_template
from flask.wrappers import Response
from pykafka import KafkaClient

def get_kafka_client():
    return KafkaClient(hosts="127.0.0.1:9092")

app = Flask(__name__)

@app.route('/')
def index():
    return render_template('index.html')

#handling cityLineBuses messages
@app.route('/topic/cityLineBuses')
def get_messages_city():
    client = get_kafka_client()
    def events():
        for message in client.topics['cityLineBuses'].get_simple_consumer():
                yield 'data:{0}\n\n'.format(message.value.decode())
    return Response(events(), mimetype="text/event-stream")


#handle circleLineBuses messages
@app.route('/topic/circleLineBuses')
def get_messages_circle():
    client = get_kafka_client()
    def events():
        for message in client.topics['circleLineBuses'].get_simple_consumer():
                yield 'data:{0}\n\n'.format(message.value.decode())
    return Response(events(), mimetype="text/event-stream")

#handle beachLineBuses messages
@app.route('/topic/beachLineBuses')
def get_messages_beach():
    client = get_kafka_client()
    def events():
        for message in client.topics['beachLineBuses'].get_simple_consumer():
                yield 'data:{0}\n\n'.format(message.value.decode())
    return Response(events(), mimetype="text/event-stream")

if __name__ == "__main__":
    app.run(debug=True, port=8080)

