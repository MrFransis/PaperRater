import cmd
import sys
from asyncio import sleep
from pymongo import MongoClient
from neo4j import GraphDatabase
import pandas as pd
import random
import time
from datetime import datetime, date, timedelta

import getPapers


class App(cmd.Cmd):
    intro = 'PaperRater Server launched. \n \nType help or ? to list commands.\n'
    prompt = '>'
    num_users = '1000'

    mongo_client = MongoClient('172.16.4.68', 27020, username='admin', password='paperRaterApp', w=3, readPreference='secondaryPreferred')
    neo4j_driver = GraphDatabase.driver("bolt://172.16.4.68:7687", auth=("neo4j", "paperRaterApp"))
    #mongo_client = MongoClient('localhost', 27017)
    #neo4j_driver = GraphDatabase.driver("bolt://localhost:7687", auth=("neo4j", "root"))


    def do_initDB(self, arg):
        'Initialize database'

        start_date = '2012-01-01'

        #papers_path = getPapers.import_data(start_date)
        papers_path = './data/papers2012-01-01.json'
        # users_path = getUsers.import_data(num_users)
        users_path = './data/users.json'

        # Initialization of utils
        session = self.neo4j_driver.session()
        users_df = pd.read_json(users_path, lines=True)
        papers_df = pd.read_json(papers_path, lines=True)

        # Converts IDs to str
        papers_df['arxiv_id'] = papers_df['arxiv_id'].map(str)
        papers_df['vixra_id'] = papers_df['vixra_id'].map(str)

        # Drop old databases
        self.mongo_client.drop_database('PaperRater')

        #query = ("CREATE OR REPLACE DATABASE neo4j")
        query = ("MATCH (n) DETACH DELETE n")
        session.write_transaction(lambda tx: tx.run(query))

        # Create new databases
        db = self.mongo_client["PaperRater"]

        ### Papers
        papers_col = db["Papers"]

        # abstract is a Java 8 keyword
        papers_df = papers_df.rename(columns={"abstract": "_abstract"})

        data_dict = papers_df.to_dict("records")

        papers_col.insert_many(data_dict)

        for index, row in papers_df.iterrows():
            query = ("CREATE (p:Paper { arxiv_id: $arxiv_id, vixra_id: $vixra_id, title: $title, category: $category,"
                     " authors: $authors, published: $published}) ")
            session.write_transaction(lambda tx: tx.run(query, arxiv_id=row['arxiv_id'], vixra_id=row['vixra_id'],
                                                        title=row['title'],category=row['category'], authors=row['authors'],
                                                        published=row['published']))

        print("Added papers to databases")

        ### Users
        users_col = db["Users"]
        data_dict = users_df.to_dict("records")
        users_col.insert_many(data_dict)

        for index, row in users_df.iterrows():
            query = ("CREATE (u:User { username: $username , email: $email}) ")
            session.write_transaction(lambda tx: tx.run(query, username=row['username'], email=row['email']))

        print("Added users to database")
       
        ### Comments
        for index, row in papers_df.iterrows():
            num_comments = int(random.random() * 10)
            comments = []
            for i in range(0, num_comments):
                now = datetime.now()
                rand_user = users_df.sample()['username'].values[0]

                comment = {'username': rand_user,
                           'text': "Commento",  # getRandom Comment
                           'timestamp': now.strftime("%Y-%m-%d %H:%M:%S")}
                comments.append(comment)

                query = (
                    "MATCH (a:User), (b:Paper) "
                    "WHERE a.username = $username AND (b.arxiv_id = $arxiv_id AND b.vixra_id = $vixra_id) "
                    "MERGE (a)-[r:HAS_COMMENTED]->(b)"
                )
                session.write_transaction(lambda tx: tx.run(query, username=rand_user,
                                                            arxiv_id=row['arxiv_id'], vixra_id=row['vixra_id']))


            db.Papers.update_one({"$and":[{'arxiv_id': row['arxiv_id']}, {'vixra_id': row['vixra_id']}]}, {'$set': {'comments': comments}})

        print("Added comments and has commented relationship to database")

        ### Reading Lists
        for index, row in users_df.iterrows():
            num_reading_lists = int(random.random() * 6)
            reading_lists = []

            # Generate a random number of reading lists
            for i in range(0, num_reading_lists):
                title = 'r_list' + str(i)
                reading_list = {'title': title}

                papers = []
                num_papers_in_reading_list = int(random.random() * 31)

                # Select a random number of papers to add to the reading list
                for j in range(0, num_papers_in_reading_list):
                    random_paper = papers_df.sample()

                    paper_to_add = {'arxiv_id': random_paper['arxiv_id'].values[0],
                                    'vixra_id': random_paper['vixra_id'].values[0],
                                    'title': random_paper['title'].values[0],
                                    'published': random_paper['published'].values[0],
                                    'authors': random_paper['authors'].values[0],
                                    'category': random_paper['category'].values[0]
                                    }

                    papers.append(paper_to_add)

                reading_list['papers'] = papers

                query = ("MATCH (a:User) "
                         "WHERE a.username = $username "
                         "CREATE (b:ReadingList { owner: $username, title: $title}) ")
                session.write_transaction(
                    lambda tx: tx.run(query, username=row['username'], title=reading_list['title']))

                n_follows = int(random.random() * 4)
                for i in range(0, n_follows):

                    while True:
                        rand_follower = users_df.sample()['username'].values[0]
                        # Users can not follow their Reading Lists
                        if rand_follower != row['username']:
                            break

                    query = (
                            "MATCH (a:User), (b:ReadingList) "
                            "WHERE a.username = $username1 AND (b.owner = $username2 AND b.title = $title) "
                            "CREATE (a)-[r:FOLLOWS]->(b)"
                    )
                    reading_lists.append(reading_list)
                    session.write_transaction(lambda tx: tx.run(query, username1=rand_follower,
                                                                username2=row['username'], title=reading_list['title']))


            result = db.Users.update_one({'username': row['username']}, {'$set': {'readingLists': reading_lists}})

        print("Added Reading Lists and Follows")

        ### User Follows and Likes
        for index, row in users_df.iterrows():
            query = (
                    "MATCH (a:User), (b:User) "
                    "WHERE a.username = $username1 AND b.username = $username2 "
                    "CREATE (a)-[r:FOLLOWS]->(b)"
            )

            n_follows = int(random.random() * 11)
            for i in range(0, n_follows):
                while True:
                    rand_user = users_df.sample()['username'].values[0]
                    # Users can not follow themselves
                    if rand_user != row['username']:
                        break
                session.write_transaction(lambda tx: tx.run(query, username1=row['username'], username2=rand_user))

            query = (
                    "MATCH (a:User), (b:Paper) "
                    "WHERE a.username = $username AND (b.arxiv_id = $arxiv_id AND b.vixra_id = $vixra_id) "
                    "CREATE (a)-[r:LIKES]->(b)"
            )

            n_follows = int(random.random() * 11)
            for i in range(0, n_follows):
                rand_paper = papers_df.sample()
                session.write_transaction(lambda tx: tx.run(query, username=row['username'],
                                                            arxiv_id=rand_paper['arxiv_id'].values[0],
                                                            vixra_id=rand_paper['vixra_id'].values[0]))


        print("Added User Follows and Likes")

        admin = {
            "username": "admin",
            "email": "admin@gmail.com",
            "password": "admin",
            "firstName": "",
            "lastName": "",
            "picture": "",
            "age": -1,
            "readingLists": [],
            "type": 2
        }
        users_col.insert_one(admin)

        query = ("CREATE (u:User { username: $username, email: $email }) ")
        session.write_transaction(lambda tx: tx.run(query, username='admin', email='admin'))
        print("Added Administrator")

        for i in range(0,5):
            username = "moderator" + str(i)
            moderator = {
                "username": username,
                "email": username + "@gmail.com",
                "password": username,
                "password": "admin",
                "firstName": "",
                "lastName": "",
                "picture": "",
                "age": -1,
                "readingLists": [],
                "type": 1
            }
            users_col.insert_one(moderator)
            query = ("CREATE (u:User { username: $username, email: $email}) ")
            session.write_transaction(lambda tx: tx.run(query, username=moderator['username'], email=moderator['email']))

        print("Added Moderators")

        session.close()


    def do_updateDB(self, arg):
        'Download latest papers'

        # Get Database
        db = self.mongo_client["PaperRater"]
        papers_col = db["Papers"]
        doc = papers_col.find().sort('published', 1).limit(1)
        for x in doc:
            last_date_uploaded = x['published']

        t = time.strptime(last_date_uploaded, '%Y-%m-%d')
        newdate = date(t.tm_year, t.tm_mon, t.tm_mday) + timedelta(1)

        last_date_uploaded = newdate.strftime('%Y-%m-%d')

        # Import latest papers
        papers_path = getPapers.import_data(last_date_uploaded)

        papers_df = pd.read_json(papers_path, lines=True)

        # Converts IDs to str
        papers_df['arxiv_id'] = papers_df['arxiv_id'].map(str)
        papers_df['vixra_id'] = papers_df['vixra_id'].map(str)

        # abstract is a Java 8 keyword
        papers_df = papers_df.rename(columns={"abstract": "_abstract"})

        data_dict = papers_df.to_dict("records")

        papers_col.insert_many(data_dict)

        session = self.neo4j_driver.session()
        for index, row in papers_df.iterrows():
            query = ("CREATE (p:Paper { arxiv_id: $arxiv_id, vixra_id: $vixra_id}) ")
            session.write_transaction(lambda tx: tx.run(query, arxiv_id=row['arxiv_id'], vixra_id=row['vixra_id']))

        print("Added papers to databases")

    def do_exit(self, arg):
        'Exit PaperRater Server'
        self.mongo_client.close()
        self.neo4j_driver.close()
        sys.exit()


if __name__ == '__main__':
    App().cmdloop()

