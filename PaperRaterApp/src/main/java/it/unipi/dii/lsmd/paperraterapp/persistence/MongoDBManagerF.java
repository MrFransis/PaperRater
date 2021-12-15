package it.unipi.dii.lsmd.paperraterapp.persistence;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.mongodb.client.*;
import com.mongodb.client.model.Accumulators;
import com.mongodb.client.model.Aggregates;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;
import org.bson.Document;
import org.bson.conversions.Bson;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.function.Consumer;

import static com.mongodb.client.model.Filters.*;
import static com.mongodb.client.model.Updates.*;
import static com.mongodb.client.model.Aggregates.*;
import static com.mongodb.client.model.Projections.*;
import static com.mongodb.client.model.Accumulators.*;
import static com.mongodb.client.model.Accumulators.addToSet;
import static com.mongodb.client.model.Accumulators.sum;
import static com.mongodb.client.model.Sorts.descending;

import it.unipi.dii.lsmd.paperraterapp.model.*;
import java.util.*;
import java.lang.reflect.Type;
import java.util.function.Consumer;
import java.util.regex.Pattern;

public class MongoDBManagerF {
    public MongoDatabase db;
    private MongoCollection usersCollection;
    private MongoCollection papersCollection;


    public MongoDBManagerF(MongoClient client) {
        this.db = client.getDatabase("PaperRater");;
        usersCollection = db.getCollection("Users");
        papersCollection = db.getCollection("Papers");
    }


    /* Create Paper */
    /**
     * Add a new paper in MongoDB
     * @param p The object Paper which contains all the necessary information about it
     * @return  true if operation is successfully executed, false otherwise
     */
    public boolean addPaper(Paper p) {
        try {
            Document doc = new Document();
            //Data conversion to string
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
            String format = formatter.format(p.getPublished());

            //Check paper source
            if(!p.getArxiv_id().isEmpty())
                doc.append("arxiv_id", p.getArxiv_id()).append("vixra_id", Float.NaN);
            else
                doc.append("arxiv_id", Float.NaN).append("vixra_id", p.getVixra_id());

            doc.append("_title", p.getTitle())
                    .append("_abstract", p.getAbstract())
                    .append("category", p.getCategory())
                    .append("authors", p.getAuthors())
                    .append("published", format)
                    //No comment on insert
                    .append("comments", null);

            papersCollection.insertOne(doc);
            return true;
        }
        catch (Exception ex)
        {
            System.err.println("Error in adding a new paper");
            ex.printStackTrace();
            return false;
        }
    }

    /*Read reading list*/
    /**
     * Returns all the Reading Lists in the database MongoDB
     * @return The list of the Reading List
     */
    public List<ReadingList> showReadingList() {
        List<ReadingList> readingLists = new ArrayList<>();
        Gson gson = new GsonBuilder().serializeNulls().create();
        Bson unwind = unwind("$reading_lists");
        Bson project = project(fields(excludeId(), computed("ReadingList", "$reading_lists")));

        MongoCursor<Document> iterator = (MongoCursor<Document>)
                usersCollection.aggregate(Arrays.asList(unwind, project)).iterator();

        Type readingListType = new TypeToken<ArrayList<ReadingList>>(){}.getType();
        while (iterator.hasNext())
        {
            Document document = iterator.next();
            System.out.println(document);
            Document ReadingListDocument = (Document) document.get("ReadingList");
            ReadingList readingList = gson.fromJson(gson.toJson(ReadingListDocument), ReadingList.class);
            readingLists.add(readingList);
        }

        return readingLists;
    }

    /*Update User*/
    /**
     * Edit an already present user
     * @param u the new user information to replace the old one
     * @return  true if operation is successfully executed, false otherwise
     */
    public boolean updateUser(User u){
        try {
            Document doc = new Document().append("username", u.getUsername());
            if (!u.getEmail().isEmpty())
                doc.append("email", u.getEmail());
            if (!u.getPassword().isEmpty())
                doc.append("password", u.getPassword());
            if (!u.getFirstName().isEmpty())
                doc.append("firstName", u.getFirstName());
            if (!u.getLastName().isEmpty())
                doc.append("lastName", u.getLastName());
            if (!u.getPicture().isEmpty())
                doc.append("picture", u.getPicture());
            if (u.getAge() != -1)
                doc.append("age", u.getAge());
            if (u.getReadingLists() != null)
                doc.append("reading_lists", u.getReadingLists());

            Bson updateOperation = new Document("$set", doc);

            usersCollection.updateOne(new Document("username", u.getUsername()), updateOperation);
            return true;
        }
        catch (Exception ex)
        {
            System.err.println("Error in updating user on MongoDB");
            return false;
        }
    }

    /*Delete Paper*/
    /**
     * Function that deletes the paper from the database
     * @param p Paper to delete
     * @return true if operation is successfully executed, false otherwise
     */
    public boolean deletePaper (Paper p) {
        try {
            if(!p.getArxiv_id().isEmpty())
                papersCollection.deleteOne(eq("arxiv_id", p.getArxiv_id()));
            else
                papersCollection.deleteOne(eq("vixra_id", p.getVixra_id()));

            return true;
        }
        catch (Exception ex)
        {
            System.err.println("Error in delete paper");
            return false;
        }
    }

    /*Search Paper by Category*/
    /**
     * Function that return the paper given the category
     * @param category Category of the papers
     * @return The list of papers
     */
    public List<Paper> getPaperByCategory(String category){

        List<Paper> papers = new ArrayList<>();
        Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd hh:mm:ss").create();
        List<Document> results = (List<Document>) papersCollection.find(eq("category", category))
                .into(new ArrayList<>());
        Type paperListType = new TypeToken<ArrayList<Paper>>(){}.getType();
        papers = gson.fromJson(gson.toJson(results), paperListType);
        return papers;
    }

    /*Search Reading Lists by User*/
    /**
     * Function that return the ReadingLists given the user
     * @param username Username of the user
     * @return  The list of reading lists
     */
    public List<ReadingList> getReadingListByUser (String username){
        List<ReadingList> readingLists = new ArrayList<>();
        Gson gson = new GsonBuilder().serializeNulls().create();

        Bson match = match(eq("username", username));
        Bson unwind = unwind("$reading_lists");
        Bson project = project(fields(excludeId(), computed("ReadingList", "$reading_lists")));
        MongoCursor<Document> iterator = (MongoCursor<Document>) usersCollection.aggregate(Arrays.asList(match, unwind, project)).iterator();
        while (iterator.hasNext())
        {
            Document document = iterator.next();
            Document ReadingListDocument = (Document) document.get("ReadingList");
            ReadingList readingList = gson.fromJson(gson.toJson(ReadingListDocument), ReadingList.class);
            readingLists.add(readingList);
        }

        return readingLists;
    }

    /*Weekly/Monthly/All time summary of the categories by number of papers Published */
    /**
     * Function that return the top categories by the number of papaer published
     * @param start initial date
     * @param finish final date
     * @param number number of results to show
     * @return The list of the most common categories
     */
    public List<String> summaryOfCategory (String start, String finish, int number){
        List<String> mostCommonCategories = new ArrayList<>();

        Bson match = match(and(gte("published", start), lte("published", finish)));
        Bson group = group("$category", sum("totalPaper", 1));
        Bson project = project(fields(excludeId(), computed("category", "$_id"), include("totalPaper")));
        Bson sort = sort(descending("totalPaper"));
        Bson limit = limit(number);

        List<Document> results = (List<Document>) papersCollection.aggregate(Arrays.asList(match, group, project, sort, limit)).into(new ArrayList<>());

        for (Document document: results)
        {
            mostCommonCategories.add(document.getString("category"));
        }
        return mostCommonCategories;
    }

    /*Users with the highest number of categories in their reading lists  */
    /**
     * Function that return a list of User with the highest number of reading lists
     * @param number First "number" users
     * @return  The list of users
     */
    public List<String> searchUsersWithHighestNumberOfCategories (int number)
    {
        List<String> mostCommonCategories = new ArrayList<>();

        Bson unwind1 = unwind("$reading_lists");
        Bson unwind2 = unwind("$reading_lists.papers");
        Bson groupMultiple = new Document("$group",
                new Document("_id", new Document("username", "$username").append("category", "$reading_lists.papers.category")));
        Bson group = group("$_id.username", sum("totalCategory", 1));
        Bson project = project(fields(excludeId(), computed("username", "$_id"), include("totalCategory")));
        Bson sort = sort(descending("totalCategory"));
        Bson limit = limit(number);

        List<Document> results = (List<Document>) usersCollection.aggregate(Arrays.asList(unwind1, unwind2, groupMultiple, group, project, sort, limit)).into(new ArrayList<>());
        for (Document document: results)
        {
            mostCommonCategories.add(document.getString("username"));
        }
        return mostCommonCategories;
    }
}
