# use python 3.10 slim image
FROM python:3.10-slim

# set working directory
WORKDIR /app

RUN apt-get update && apt-get install -y gcc musl-dev procps

# copy requirements.txt
COPY requirements.txt .
RUN pip install -r requirements.txt

# copy file
COPY . .

# set port
EXPOSE 5000

# run the main.py
CMD ["python", "main.py"]
