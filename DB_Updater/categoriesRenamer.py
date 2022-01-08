import numpy as np


def rename_categories(df):
    """
    Function that renames viXra and arXiv categories so that the corresponding category names are equal
    :param DataFrame:
    """
    print("Renaming categories ...")

    ### Physics
    # viXra
    df['category'].replace({"Quantum Gravity and String Theory": "Quantum Physics",
                            "Relativity and Cosmology": "General Relativity and Quantum Cosmology",
                            "Thermodynamics and Energy": "Physics",
                            "Geophysics": "Physics",
                            "Climate Research": "Physics",
                            "History and Philosophy of Physics": "Physics"},
                           inplace=True)

    # arXiv
    df.category = np.where(df.category.str.startswith('astro'), "Astrophysics",
                           (np.where(df.category.str.startswith('cond'), "Condensed Matter",
                            (np.where(df.category.str.startswith('gr'), "General Relativity and Quantum Cosmology",
                             (np.where(df.category.str.startswith('hep'), "High Energy Particle Physics",
                              (np.where(df.category.str.startswith('math'), "Mathematical Physics",
                               (np.where(df.category.str.startswith('nlin'), "Nonlinear Sciences",
                                (np.where(df.category.str.startswith('nucl'), "Nuclear and Atomic Physics",
                                 (np.where(df.category.str.startswith('physics'), "Physics",
                                  (np.where(df.category.str.startswith('quant'), "Quantum Physics", df.category)))))))))))))))))

    ### Mathematics
    #viXra
    df['category'].replace({"Set Theory":"Mathematics",
                            "Number Theory":"Mathematics",
                            "Combinatorics and Graph Theory":"Mathematics",
                            "Algebra":"Mathematics",
                            "Geometry":"Mathematics",
                            "Topology":"Mathematics",
                            "Functions and Analysis":"Mathematics",
                            "General Mathematics":"Mathematics"},
                            inplace=True)

    # arXiv
    df.category = np.where(df.category.str.startswith('math'), "Mathematics", df.category)

    ### Computational Science
    #viXra
    df['category'].replace({"Digital Signal Processing":"Computational Science",
                            "Data Structures and Algorithms":"Computational Science",
                            "Artificial Intelligence":"Computational Science"},
                            inplace=True)

    # arXiv
    df.category = np.where(df.category.str.startswith('cs'), "Computational Science", df.category)

    ### Biology
    # arXiv
    df.category = np.where(df.category.str.startswith('q-bio'), "Quantitative Biology", df.category)

    # Chemistry (invariate)

    ### Statistics
    # arXiv
    df.category = np.where(df.category.str.startswith('stat'), "Statistics", df.category)

    ### Electrical Engineering and Systems Science
    # arXiv
    df.category = np.where(df.category.str.startswith('eess'), "Electrical Engineering and Systems Science", df.category)

    ### Humanities
    # arXiv
    df.category = np.where(df.category.str.startswith('q-fin'), "Economics and Finance",
                           (np.where(df.category.str.startswith('econ'), "Economics and Finance", df.category)))

    return df