import pandas as pd
import requests
from bs4 import BeautifulSoup
from datetime import date, timedelta
from dateutil.relativedelta import relativedelta

url = 'https://vixra.org/all/'
headers = {
    'User-Agent': 'Mozilla/5.0 (Macintosh; Intel Mac OS X 10.12; rv:55.0) Gecko/20100101 Firefox/55.0',
}


def get_data(start_date):
    """
    get_data queries viXra in order to retrieve metadata associated with all the papers that were published onto
    viXra since the start_date.
    """
    print("Downloading viXra papers ...")
    year, month, day = start_date.split('-')
    year, month, day = int(year), int(month), int(day)
    stop_date = date(year, month, day)
    curr_date = date.today()
    df = pd.DataFrame(columns=('vixra_id', 'title', 'abstract', 'category', 'authors', 'published'))

    while curr_date >= stop_date:
        print(curr_date)
        year = curr_date.year
        month = curr_date.month
        cd = str(year)[2:4].zfill(2) + str(month).zfill(2)

        r = requests.get(url + cd, headers=headers)
        soup = BeautifulSoup(r.content.decode('utf-8', 'surrogatepass'), 'html.parser')

        papers = soup.find("div", {"id": "flow"})

        for p, a in zip(papers.find_all("p", recursive=False), papers.find_all("div", {"id": "abstract"})):
            vixra_id = p.find("a").text.split(":")[1]

            published = p.find("i").text.split(" ")[2]

            # if the paper published date is older than the start date then stop the loop
            year, month, day = published.split('-')
            year, month, day = int(year), int(month), int(day)
            temp = date(year, month, day)
            if temp < stop_date:
                break

            title = a.find("h3").text

            authors = []
            for elem in a.find("p").find_all("a"):
                authors.append(elem.text)

            try:
                abstract = a.find_all("p", recursive=False)[1].find(text=True).strip()
                    # Avoid bad characters
                abstract = abstract.encode('utf-8', 'replace').decode()
                primary_category = a.find_all("p", recursive=False)[1].find("a").text

            except IndexError:
                continue
            except AttributeError:
                continue
            except ValueError:
                continue

            contents = {'vixra_id': vixra_id,
                        'title': title,
                        'abstract': abstract,
                        'category': primary_category,
                        'authors': authors,
                        'published': published
                        }
            df = df.append(contents, ignore_index=True)

        curr_date = curr_date - relativedelta(months=1)

        df = df.drop_duplicates(subset=["vixra_id"], keep="first")

        df.to_json("./data/viXra" + start_date + ".json", orient='records', lines=True)

    return df


if __name__ == "__main__":
    start_date = "2011-01-01"

    data = pd.DataFrame()

    try:
        data = data.append(get_data(start_date))

        data = data.drop_duplicates(subset=["vixra_id"], keep="first")
        path = "./data/viXra" + start_date + ".json"
        data.to_json(path, orient='records', lines=True)
        print("Saved Json file for papers from ", start_date, " until today")

    except Exception as e:
        print(e)
