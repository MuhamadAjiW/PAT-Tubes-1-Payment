import requests
import datetime
import random

def add_invoice(email, event_id, ticket_id):
    url = "http://localhost:8080/api/payments"

    data = {
        "email": email,
        "eventId": event_id,
        "ticketId": ticket_id
    }
    headers = {'Content-Type': 'application/json'}

    response = requests.post(url, json=data, headers=headers)
    return response

def pay(invoiceNumber, endpoint):
    url = "http://localhost:8080" + endpoint

    data = {
        "invoiceNumber": invoiceNumber,
    }
    headers = {'Content-Type': 'application/json'}

    response = requests.post(url, json=data, headers=headers)
    return response

if __name__ == "__main__":
    response = add_invoice("Test@email.com", 1, 1)
    # response = pay("INVFC04B978-8FCC-44CA-AB50-CAA28B34959C",
                    # "/api/payments/pay?signature=VepxOcb2MZpYwVgA1K6vYsHEAYdGN2chMiyaqaRstwMnY6cPrEat9invV3yfMU_jwuSg--pJGycWRfkhLDsU3lgC13AtYndHbnr60Z4ti1XCPGgbYv7-9-fegiIQFviA")

    print("\nstatus code:", response.status_code)
    print("Response content:")
    print(response.text)
