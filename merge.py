import pandas as pd
import numpy as np

def merge_data(path_arXiv, path_viXra):
    """
    merge_data takes as first argument the path of arXiv data and as second argument the path of viXra data
    returns the ordered and concatenated data
    """
    data_arXiv = pd.read_json(path_arXiv, lines=True)
    data_arXiv.drop(['arxiv_id', 'categories', 'updated', 'doi'], axis=1, inplace=True)
    data_arXiv['published'] = pd.to_datetime(data_arXiv['published'])

    data_viXra = pd.read_json(path_viXra, lines=True)
    data_viXra.drop(['vixra_id'], axis=1, inplace=True)
    data_viXra['published'] = pd.to_datetime(data_viXra['published'])

    #Problem on authors format: arXiv list, viXra string
    data_merge = pd.DataFrame(np.concatenate((data_arXiv.values, data_viXra.values), axis=0))
    data_merge.columns = ['title', 'abstract', 'category', 'authors', 'published']

    data_merge.sort_values(by=['published'], inplace=True)

    #How delete duplicate?
    #data_merge.drop_duplicates(subset=["title"], keep="first", inplace=True)

    return data_merge


if __name__ == "__main__":
    path_arXiv = "data/arXiv2021-11-23.json"
    path_viXra = "data/viXra2021-11-23.json"
    data = pd.DataFrame()

    data = data.append(merge_data(path_arXiv, path_viXra))
    data.to_json("./data/merge.json", orient='records', lines=True)

