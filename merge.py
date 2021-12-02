import pandas as pd
import arXivMetadataScraper
import viXraMetadataScraper

def rename_categories(DataFrame df):

    df['category'].replace({"High Energy Particle Physics":"Physics",
                            "Quantum Gravity and String Theory":"Physics",
                            "Relativity and Cosmology":"Physics",
                            "Astrophysics":"Physics",
                            "Quantum Physics":"Physics",
                            "Nuclear and Atomic Physics":"Pyhsics",
                            "Condensed Matter":"Physics",
                            "Thermodynamics and Energy":"Physics",
                            "Classical Physics":"Physics",
                            "Geophysics":"Physics",
                            "Climate Research":"Physics",
                            "Mathematical Physics":"Physics"}, inplace=True)




"""High Energy Particle Physics (1812) - particle theory, phenomenology and experiment that ignores gravity
Quantum Gravity and String Theory (1870) - and any other unified theories or "TOEs" that include gravity
Relativity and Cosmology (4244) - classical physics of special and general relativity including cosmology and application in astrophysics
Astrophysics (2691) - astronomical physics covering planetary, stellar and galactic systems.
Quantum Physics (4609) - other aspects of quantum physics including quantum information theory
Nuclear and Atomic Physics (719)
Condensed Matter (1513)
Thermodynamics and Energy (408) - statistical mechanics
Classical Physics (1886) - and other general physics topics outside relativity and quantum theory
Geophysics (256)
Climate Research (123)
Mathematical Physics (1249) - mathematical research related to physics
History and Philosophy of Physics (480) - including sociology of science"""

def merge_data(path_arXiv, path_viXra):
    """
    merge_data takes as first argument the path of arXiv data and as second argument the path of viXra data
    returns the ordered and concatenated data
    """
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
    arXivMetadataScraper.get_data(start_date)
    viXraMetadataScraper.get_data(start_date)

    path_arXiv = "data/arXiv" + start_date + ".json"
    path_viXra = "data/viXra" + start_date + ".json"

    data = pd.DataFrame()
    data = data.append(merge_data(path_arXiv, path_viXra))
    rename_categories(data)
    data.to_json("./data/merge" + start_date + ".json", orient='records', lines=True)
    print("Saved merged Json file for papers from ", start_date, " until today")

if __name__ == "__main__":
    start_date = "2021-11-30"
    import_data(start_date)