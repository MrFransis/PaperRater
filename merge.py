import pandas as pd
import numpy as np
import arXivMetadataScraper
import viXraMetadataScraper

def merge_data(path_arXiv, path_viXra):
    """
    merge_data takes as first argument the path of arXiv data and as second argument the path of viXra data
    returns the ordered and concatenated data
    """
    data_arXiv = pd.read_json(path_arXiv, lines=True)
    data_arXiv.drop(['categories', 'updated', 'doi'], axis=1, inplace=True)

    data_viXra = pd.read_json(path_viXra, lines=True)

    data_merge = pd.DataFrame(np.concatenate((data_arXiv.values, data_viXra.values), axis=0))
    data_merge.columns = ['id', 'title', 'abstract', 'category', 'authors', 'published']

    data_merge.sort_values(by=['published'], inplace=True)

    #data_merge.drop_duplicates(subset=["title"], keep="first", inplace=True)

    return data_merge

def import_data(start_date):
    arXivMetadataScraper.get_data(start_date)
    viXraMetadataScraper.get_data(start_date)

    path_arXiv = "data/arXiv" + start_date + ".json"
    path_viXra = "data/viXra" + start_date + ".json"

    data = pd.DataFrame()
    data = data.append(merge_data(path_arXiv, path_viXra))
    data.to_json("./data/merge" + start_date + ".json", orient='records', lines=True)

if __name__ == "__main__":
    start_date = "2021-11-10"
    import_data(start_date)


