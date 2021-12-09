from datetime import date
import pandas as pd
import arxiv


def get_data_by_category(category, start_date):
    """
    get_data queries arXiv in order to retrieve metadata associated with all the papers that were published onto
    arXiv since the start_date.
    """
    year, month, day = start_date.split('-')
    year, month, day = int(year), int(month), int(day)
    st_d = date(year, month, day)

    df = pd.DataFrame(columns=('arxiv_id', 'title', 'abstract', 'category', 'categories',
                               'authors', 'published', 'updated', 'doi'))

    search = arxiv.Search(
        query=category,
        max_results=float('inf'),
        sort_by=arxiv.SortCriterion.SubmittedDate
    )

    for result in search.results():

        """
        ArXiv API does not support temporal queries, so the loop is stopped when the published date of
        the last paper retrieved is older than the start_date
        """
        authors = []
        for author in result.authors:
            authors.append(str(author))

        if result.published.date() < st_d:
            break

        # extracting only the paper id
        arxiv_id = result.entry_id.split("/")[4][:10]

        contents = {'arxiv_id': arxiv_id,
                    'title': result.title,
                    'abstract': result.summary.strip(),
                    'category': result.primary_category,
                    'categories': result.categories,
                    'authors': authors,
                    'published': str(result.published.date()),
                    'updated': str(result.updated.date()),
                    'doi': result.doi
                    }
        df = df.append(contents, ignore_index=True)

    return df


def get_data(start_date):
    print("Downloading arXiv papers ...")
    categories = ['eess', 'econ', 'math', 'cs', 'physics', 'physics:astro-ph', 'physics:cond-mat', 'physics:gr-qc',
                  'physics:hep-ex', 'physics:hep-lat', 'physics:hep-ph', 'physics:hep-th', 'physics:math-ph',
                  'physics:nlin', 'physics:nucl-ex', 'physics:nucl-th', 'physics:physics', 'physics:quant-ph', 'q-bio',
                  'q-fin', 'stat']

    data = pd.DataFrame()

    try:
        for category in categories:
            try:
                data = data.append(get_data_by_category(category, start_date))
            except Exception as e:
                print(e)
                pass

        data = data.drop_duplicates(subset=["arxiv_id"], keep="first")

        path = "./data/arXiv" + start_date + ".json"
        data.to_json(path, orient='records', lines=True)
        print("Saved Json file for papers from ", start_date, " until today")

    except Exception as e:
        print(e)


if __name__ == "__main__":
    start_date = "2021-11-23"
    get_data(start_date)
