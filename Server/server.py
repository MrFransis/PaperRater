import cmd
import sys
from asyncio import sleep
from pymongo import MongoClient
import pandas as pd
import random
from datetime import datetime


class App(cmd.Cmd):
    intro = 'PaperRater Server launched. \n \nType help or ? to list commands.\n'
    prompt = '>'

    start_date = '2021-11-30'
    num_users = '1000'
    client = MongoClient('localhost', 27017)
    # papers_path = getPapers.import_data(start_date)
    papers_path = './data/papers2021-11-30.json'
    # users_path = getUsers.import_data(num_users)
    users_path = './data/users.json'


    def do_initDB(self, arg):
        'Initialize the database'
        # Drop old database
        self.client.drop_database('PaperRater')

        db = self.client["PaperRater"]

        ### Papers
        papers_col = db["Papers"]
        papers_df = pd.read_json(self.papers_path, lines=True)

        # Converts IDs to str
        papers_df['arxiv_id'] = papers_df['arxiv_id'].map(str)
        papers_df['vixra_id'] = papers_df['vixra_id'].map(str)
        data_dict = papers_df.to_dict("records")
        papers_col.insert_many(data_dict)
        print("Added papers to database")

        ### Users
        users_col = db["Users"]
        users_df = pd.read_json(self.users_path, lines=True)
        data_dict = users_df.to_dict("records")
        users_col.insert_many(data_dict)
        print("Added users to database")

        ### Comments
        for paper in papers_col.find():
            num_comments = int(random.random() * 10)
            comments = []
            for i in range(0, num_comments):
                now = datetime.now()

                comment = {'username': users_df.sample()['username'].values[0],
                           'comment': "Commento",  # getRandom Comment
                           'timestamp': now.strftime("%Y-%m-%d %H:%M:%S")}
                comments.append(comment)

            db.Papers.update_one({'_id': paper['_id']}, {'$set': {'comments': comments}})

        print("Added comments to database")

        ### Reading Lists
        for user in users_col.find():
            num_reading_lists = int(random.random() * 6)
            reading_lists = []

            # Generate a random number of reading lists
            for i in range(0, num_reading_lists):
                reading_list = []

                num_papers_in_reading_list = int(random.random() * 31)

                # Select a random number of papers to add to the reading list
                for j in range(0, num_papers_in_reading_list):
                    random_paper = papers_df.sample()

                    paper_to_add = {'arxiv_id': random_paper['arxiv_id'].values[0],
                                    'vixra_id': random_paper['vixra_id'].values[0],
                                    'title': random_paper['title'].values[0],
                                    'authors': random_paper['authors'].values[0],
                                    }

                    reading_list.append(paper_to_add)

                reading_lists.append(reading_list)

                result = db.Users.update_one({'_id': user['_id']}, {'$set': {'reading_lists': reading_lists}})

        print("Added Reading Lists to the database")


    def do_exit(self, arg):
        'Exit PaperRater Server'
        sys.exit()

if __name__ == '__main__':
    App().cmdloop()