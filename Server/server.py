import pandas as pd
import random
from datetime import datetime

import getPapers
import getUsers


def uploadData(path, collection):
    data = pd.read_json(path, lines=True)
    data_dict = data.to_dict("records")
    collection.insert_many(data_dict)

def uploadComments():
    from pymongo import MongoClient
    client = MongoClient('localhost', 27017)

    db = client["PaperRater"]
    papers_col = db["Papers"]

    # Users dataframe
    users_df = pd.read_json('./data/users.json', lines=True)

    for paper in papers_col.find():
        num_comments = int(random.random()*10)
        comments = []
        for i in range(0, num_comments):
            now = datetime.now()

            comment = {'username': users_df.sample()['username'].values[0],
                       'comment': "Commento",   # getRandom Comment
                       'timestamp': now.strftime("%Y-%m-%d %H:%M:%S")}
            comments.append(comment)

        result = db.Papers.update_one({'_id': paper['_id']}, {'$set': {'comments': comments}})


def init(papers_path, users_path):
    from pymongo import MongoClient
    client = MongoClient('localhost', 27017)
    client.drop_database('PaperRater')

    db = client["PaperRater"]

    papers_col = db["Papers"]
    users_col = db["Users"]

    uploadData(papers_path, papers_col)
    print("Added papers to database")

    uploadData(users_path, users_col)
    print("Added users to database")

    uploadComments()
    print("Added comments to database")

if __name__ == "__main__":

    start_date = '2021-11-30'
    num_users = '1000'

    papers_path = getPapers.import_data(start_date)
    users_path = getUsers.import_data(num_users)

    init(papers_path, users_path)

   # while True:
        # daily update routine