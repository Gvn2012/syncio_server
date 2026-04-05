FROM python:3.12-slim

WORKDIR /app

ENV PYTHONDONTWRITEBYTECODE=1
ENV PYTHONUNBUFFERED=1

COPY telegram_bot/requirements.txt requirements.txt
RUN pip install --no-cache-dir -r requirements.txt

COPY telegram_bot/app.py app.py

CMD ["python", "app.py"]
