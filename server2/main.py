import subprocess
import netifaces as ni

from flask import Flask, jsonify

# Press ⌃R to execute it or replace it with your code.
# Press Double ⇧ to search everywhere for classes, files, tool windows, actions, and settings.

app = Flask(__name__)

@app.route('/', methods=['GET'])
def info():
    return jsonify({
        "Service2": {
        'IP address information': get_container_ip(),
        'list of running processes': subprocess.check_output(["ps", "-ax"]).decode("utf-8"),
        'available disk space': subprocess.check_output(["df"]).decode("utf-8"),
        'time since last boot': subprocess.check_output(["uptime"]).decode("utf-8")
        }
    })


def get_container_ip():
    interfaces = ni.interfaces()
    for interface in interfaces:
        try:
            addr = ni.ifaddresses(interface)[ni.AF_INET][0]['addr']
            if addr != "127.0.0.1":
                return addr
        except (KeyError, IndexError):
            continue
    return "127.0.0.1"

# Press the green button in the gutter to run the script.
if __name__ == '__main__':
    print('server2')
    # app.run(host='127.0.0.1', port=5000)
    app.run(host='0.0.0.0', port=5000)
    app.debug=1

