import requests as req
import json

def import_data(num):
    """Function that make a request to the server to load a {num} of users."""

    CONST_FIELDS = "gender,login,email,name,dob,picture"
    payloads = {"results": num, "inc": CONST_FIELDS}
    r = req.get("https://randomuser.me/api/", params=payloads).json()
    with open('user.json', 'a') as f:
        for result in r['results']:
            data = {}
            data['username'] = result['login']['username']
            data['email'] = result['email']
            data['password'] = result['login']['password']
            data['firstName'] = result['name']['first']
            data['lastName'] = result['name']['last']
            data['picture'] = result['picture']['medium']
            data['age'] = result['dob']['age']
            json.dump(data, f)


if __name__ == "__main__":
    num = input("Number of dummy users:\n")
    import_data(num)