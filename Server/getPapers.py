import pandas as pd
import arXivMetadataScraper
import viXraMetadataScraper
import categoriesRenamer


def merge_data(path_arXiv, path_viXra):
    """
    merge_data takes as first argument the path of arXiv data and as second argument the path of viXra data
    returns the ordered and concatenated data
    """
    print("Merging ...")
    data_merge = pd.DataFrame(columns=('arxiv_id', 'vixra_id', 'title', 'abstract', 'category',
                               'authors', 'published'))

    data_arXiv = pd.read_json(path_arXiv, lines=True)
    data_arXiv.drop(['categories', 'updated', 'doi'], axis=1, inplace=True)

    data_viXra = pd.read_json(path_viXra, lines=True)

    data_merge = data_merge.append(data_arXiv)
    data_merge = data_merge.append(data_viXra)

    data_merge.sort_values(by=['published'], inplace=True)

    return data_merge

def import_data(start_date):
    print("Importing papers data  ...")
    arXivMetadataScraper.get_data(start_date)
    viXraMetadataScraper.get_data(start_date)

    path_arXiv = "data/arXiv" + start_date + ".json"
    path_viXra = "data/viXra" + start_date + ".json"

    data = pd.DataFrame()
    data = data.append(merge_data(path_arXiv, path_viXra))
    data = categoriesRenamer.rename_categories(data)

    path = "./data/papers" + start_date + ".json"
    data.to_json(path, orient='records', lines=True)
    print("Saved merged Json file for papers from ", start_date, " until today")

    return path

if __name__ == "__main__":
    start_date = "2021-11-30"
    import_data(start_date)