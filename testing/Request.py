import requests
import pika
import json

basehost = "localhost"
baseport = "8080"
baseurl = "http://" + basehost + ":" + baseport

rabbitmqHost = "localhost"
rabbitmqPort = 8200

## Direct urls
def add_invoice(email, event_id, ticket_id):
    url = baseurl + "/api/payments"

    data = {
        "email": email,
        "eventId": event_id,
        "ticketId": ticket_id
    }
    headers = {'Content-Type': 'application/json'}

    response = requests.post(url, json=data, headers=headers)
    return response

def pay(invoiceNumber, endpoint):
    url = baseurl + endpoint

    data = {
        "invoiceNumber": invoiceNumber,
    }
    headers = {'Content-Type': 'application/json'}

    response = requests.post(url, json=data, headers=headers)
    return response



## RabbitMQ
def add_invoice_to_queue(email, event_id, ticket_id):
    rabbitmq_credentials = pika.PlainCredentials('test-user', 'test-user')
    connection = pika.BlockingConnection(pika.ConnectionParameters(rabbitmqHost, rabbitmqPort, '/', rabbitmq_credentials))
    channel = connection.channel()

    data = {
        "email": email,
        "eventId": event_id,
        "ticketId": ticket_id
    }

    channel.basic_publish(
        exchange='payment-exchange',
        routing_key='incoming-invoice-queue',
        body=json.dumps(data),
        properties=pika.BasicProperties(
            delivery_mode=2,  # Make the message persistent
        )
    )

    print(" [x] Sent invoice data to RabbitMQ")

    connection.close()

if __name__ == "__main__":
    # response = add_invoice("Test@email.com", 1, 1)
    # response = pay("INVFC04B978-8FCC-44CA-AB50-CAA28B34959C",
                    # "/api/payments/pay?signature=VepxOcb2MZpYwVgA1K6vYsHEAYdGN2chMiyaqaRstwMnY6cPrEat9invV3yfMU_jwuSg--pJGycWRfkhLDsU3lgC13AtYndHbnr60Z4ti1XCPGgbYv7-9-fegiIQFviA")

    add_invoice_to_queue("Test@email.com", 1, 1)            

    # print("\nstatus code:", response.status_code)
    # print("Response content:")
    # print(response.text)
