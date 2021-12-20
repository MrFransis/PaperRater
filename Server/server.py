import cmd
import sys
from asyncio import sleep
from pymongo import MongoClient
from neo4j import GraphDatabase
import pandas as pd
import random
from datetime import datetime


class App(cmd.Cmd):
    intro = 'PaperRater Server launched. \n \nType help or ? to list commands.\n'
    prompt = '>'

    start_date = '2021-11-30'
    num_users = '1000'
    mongo_client = MongoClient('localhost', 27017)
    neo4j_driver = GraphDatabase.driver("bolt://localhost:7687", auth=("neo4j", "root"))
    # papers_path = getPapers.import_data(start_date)
    papers_path = './data/papers2021-11-30.json'
    # users_path = getUsers.import_data(num_users)
    users_path = './data/users.json'


    def do_initDB(self, arg):
        'Initialize database'

        # Initialization of utils
        session = self.neo4j_driver.session()
        users_df = pd.read_json(self.users_path, lines=True)
        papers_df = pd.read_json(self.papers_path, lines=True)

        # Converts IDs to str
        papers_df['arxiv_id'] = papers_df['arxiv_id'].map(str)
        papers_df['vixra_id'] = papers_df['vixra_id'].map(str)

        # Drop old databases
        self.mongo_client.drop_database('PaperRater')
        query = ("CREATE OR REPLACE DATABASE neo4j")
        session.write_transaction(lambda tx: tx.run(query))

        # Create new databases
        db = self.mongo_client["PaperRater"]

        ### Papers
        papers_col = db["Papers"]
        data_dict = papers_df.to_dict("records")
        papers_col.insert_many(data_dict)

        for index, row in papers_df.iterrows():
            query = ("CREATE (p:Paper { arxiv_id: $arxiv_id, vixra_id: $vixra_id}) ")
            session.write_transaction(lambda tx: tx.run(query, arxiv_id=row['arxiv_id'], vixra_id=row['vixra_id']))

        print("Added papers to databases")

        ### Users
        users_col = db["Users"]
        data_dict = users_df.to_dict("records")
        users_col.insert_many(data_dict)

        for index, row in users_df.iterrows():
            query = ("CREATE (u:User { name: $username }) ")
            session.write_transaction(lambda tx: tx.run(query, username=row['username']))

        print("Added users to database")
       
        ### Comments
        for paper in papers_col.find():
            num_comments = int(random.random() * 10)
            comments = []
            for i in range(0, num_comments):
                now = datetime.now()
                rand_user = users_df.sample()['username'].values[0]

                comment = {'username': rand_user,
                           'comment': "Commento",  # getRandom Comment
                           'timestamp': now.strftime("%Y-%m-%d %H:%M:%S")}
                comments.append(comment)

                query = (
                    "MATCH (a:User), (b:Paper) "
                    "WHERE a.name = $username AND (b.arxiv_id = $arxiv_id AND b.vixra_id = $vixra_id) "
                    "MERGE (a)-[r:HAS_COMMENTED]->(b)"
                )
                session.write_transaction(lambda tx: tx.run(query, username=rand_user,
                                                            arxiv_id=paper['arxiv_id'], vixra_id=paper['vixra_id']))


            db.Papers.update_one({'_id': paper['_id']}, {'$set': {'comments': comments}})

        print("Added comments and has commented relationship to database")

        ### Reading Lists
        for user in users_col.find():
            num_reading_lists = int(random.random() * 6)
            reading_lists = []

            # Generate a random number of reading lists
            for i in range(0, num_reading_lists):
                reading_list = {'title': 'r_list' + str(i)}

                papers = []
                num_papers_in_reading_list = int(random.random() * 31)

                # Select a random number of papers to add to the reading list
                for j in range(0, num_papers_in_reading_list):
                    random_paper = papers_df.sample()

                    paper_to_add = {'arxiv_id': random_paper['arxiv_id'].values[0],
                                    'vixra_id': random_paper['vixra_id'].values[0],
                                    'title': random_paper['title'].values[0],
                                    'authors': random_paper['authors'].values[0],
                                    'category': random_paper['category'].values[0]
                                    }

                    papers.append(paper_to_add)

                reading_list['papers'] = papers
                reading_lists.append(reading_list)

                query = ("CREATE (r:ReadingList { username: $username, title: $title}) ")
                session.write_transaction(lambda tx: tx.run(query, username=user['username'], title=reading_list['title']))

                n_follows = int(random.random() * 4)
                for i in range(0, n_follows):
                    rand_follower = users_df.sample()['username'].values[0]
                    query = (
                            "MATCH (a:User), (b:ReadingList) "
                            "WHERE a.name = $username1 AND (b.username = $username2 AND b.title = $title) "
                            "CREATE (a)-[r:FOLLOWS]->(b)"
                    )
                    session.write_transaction(lambda tx: tx.run(query, username1=rand_follower,
                                                                username2=user['username'], title=reading_list['title']))


            result = db.Users.update_one({'_id': user['_id']}, {'$set': {'readingLists': reading_lists}})

        print("Added Reading Lists and Reading List Follows")

        ### User Follows and Likes
        for index, row in users_df.iterrows():
            query = (
                    "MATCH (a:User), (b:User) "
                    "WHERE a.name = $username1 AND b.name = $username2 "
                    "CREATE (a)-[r:FOLLOWS]->(b)"
            )

            n_follows = int(random.random() * 11)
            for i in range(0, n_follows):
                rand_user = users_df.sample()['username'].values[0]
                session.write_transaction(lambda tx: tx.run(query, username1=row['username'], username2=rand_user))

            query = (
                    "MATCH (a:User), (b:Paper) "
                    "WHERE a.name = $username AND (b.arxiv_id = $arxiv_id AND b.vixra_id = $vixra_id) "
                    "CREATE (a)-[r:LIKES]->(b)"
            )

            n_follows = int(random.random() * 11)
            for i in range(0, n_follows):
                rand_paper = papers_df.sample()
                session.write_transaction(lambda tx: tx.run(query, username=row['username'],
                                                            arxiv_id=rand_paper['arxiv_id'].values[0],
                                                            vixra_id=rand_paper['vixra_id'].values[0]))

        print("Added User Follows and Likes")

        session.close()


    def do_exit(self, arg):
        'Exit PaperRater Server'
        self.mongo_client.close()
        self.neo4j_driver.close()
        sys.exit()


if __name__ == '__main__':
    App().cmdloop()

