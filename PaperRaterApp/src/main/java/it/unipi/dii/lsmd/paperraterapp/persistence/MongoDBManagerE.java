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
import javafx.util.Pair;
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

public class MongoDBManagerE {
    public MongoDatabase db;
    private MongoCollection usersCollection;
    private MongoCollection papersCollection;


    public MongoDBManagerE(MongoClient client) {
        this.db = client.getDatabase("PaperRater");;
        usersCollection = db.getCollection("Users");
        papersCollection = db.getCollection("Papers");
    }

    public boolean createUserDocument(User u) {
        try {
            Document doc = new Document("username", u.getUsername())
                                .append("email", u.getEmail())
                                .append("password", u.getPassword());

            if (!u.getFirstName().isEmpty())
                doc.append("firstName", u.getFirstName());
            if (!u.getLastName().isEmpty())
                doc.append("lastName", u.getLastName());
            if (!u.getPicture().isEmpty())
                doc.append("picture", u.getPicture());
            if (u.getAge() != -1)
                doc.append("age", u.getAge());

            usersCollection.insertOne(doc);
            return true;

        } catch (Exception e) {
            System.out.println("Error in adding a new user");
            e.printStackTrace();
            return false;
        }
    }

    public Paper getPaperById(String id) {
        try {
            Paper p = null;
            Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd hh:mm:ss").create();
            Document myDoc = (Document) papersCollection.find(
                    or(eq("arxiv_id", id), eq("vixra_id", id))).first();
            p = gson.fromJson(gson.toJson(myDoc), Paper.class);
            return p;
        }
        catch (JsonSyntaxException e) {
            e.printStackTrace();
            return null;
        }
    }

    public boolean addPaperToReadingList(ReadingList r, Paper p) {
        try {
            Document paperReduced = new Document("arxiv_id", p.getArxiv_id())
                    .append("vixra_id", p.getVixra_id())
                    .append("title", p.getTitle())
                    .append("auhtors", p.getAuthors())
                    .append("category", p.getCategory());

            Bson find = and(eq("username", r.getUsername()),
                            eq("reading_lists.title", r.getName()));
            Bson update = Updates.addToSet("reading_lists.$.papers", paperReduced);
            usersCollection.updateOne(find, update);
            return true;
        }
        catch (Exception e)
        {
            System.out.println("Error in adding a paper to a Reading List");
            e.printStackTrace();
            return false;
        }
    }

    public boolean removePaperFromReadingList(ReadingList r, Paper p) {
        try {
            Document paperReduced = new Document("arxiv_id", p.getArxiv_id())
                    .append("vixra_id", p.getVixra_id())
                    .append("title", p.getTitle())
                    .append("auhtors", p.getAuthors());

            Bson find = and(eq("username", r.getUsername()),
                    eq("reading_lists.title", r.getName()));
            Bson delete = Updates.pull("reading_lists.$.papers", paperReduced);
            usersCollection.updateOne(find, delete);
            return true;
        }
        catch (Exception e)
        {
            System.out.println("Error in removing a paper from a Reading List");
            e.printStackTrace();
            return false;
        }
    }

    /*
    public boolean deletePaper(Paper p) {

    }
    */

    public List<Paper> searchPapersByTitle (String title) {
        List<Paper> papers = new ArrayList<>();
        Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd hh:mm:ss").create();
        Pattern pattern = Pattern.compile("^.*" + title + ".*$", Pattern.CASE_INSENSITIVE);
        Bson match = Aggregates.match(Filters.regex("title", pattern));
        Bson sort = sort(descending("published"));

        /*
        Bson limit = limit(10);
        Bson skip = skip();
         */

        List<Document> results = (List<Document>) papersCollection.aggregate(Arrays.asList(match, sort))
                .into(new ArrayList<>());
        Type papersListType = new TypeToken<ArrayList<Paper>>(){}.getType();
        papers = gson.fromJson(gson.toJson(results), papersListType);
        return papers;
    }

    public List<Paper> searchPapersByPublicationDate (String start_date, String end_date) {
        List<Paper> papers = new ArrayList<>();
        Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd hh:mm:ss").create();

        Bson match = match(and(
                Filters.gte("published", start_date),
                Filters.lte("published", end_date)));
        Bson sort = sort(descending("published"));

        /*
        Bson limit = limit(10);
        Bson skip = skip();
         */

        List<Document> results = (List<Document>) papersCollection.aggregate(Arrays.asList(match, sort))
                .into(new ArrayList<>());
        Type papersListType = new TypeToken<ArrayList<Paper>>(){}.getType();
        papers = gson.fromJson(gson.toJson(results), papersListType);
        return papers;
    }


    /* Mongo DB
    db.Users.aggregate([
  { $match: {username: "crazymouse258"}},
  { $unwind: "$reading_lists"},
  { $match: {"reading_lists.title" : "r_list0"}},
  { $unwind: "$reading_lists.papers"},
  { $group: { _id: "$reading_lists.papers.category", nPapers: {$sum: 1}}},
  { $project: {_id:0, category: "$_id", nPapers: 1}}
  ])
     */

    public String getMostCommonCategoryInReadingList(ReadingList r) {

        Bson match1 = match(Filters.eq("username", r.getUsername()));
        Bson unwind1 = unwind("$reading_lists");
        Bson match2 = match(Filters.eq("reading_lists.title", r.getName()));
        Bson unwind2 = unwind("$reading_lists.papers");
        Bson group = group("$reading_lists.papers.category",
                sum("nPapers", 1));
        Bson sort = sort(descending("nPapers"));
        Bson project = project(fields(computed("category", "$_id"),
                excludeId(), include("nPapers")));

        Document doc = (Document) usersCollection.aggregate(
                Arrays.asList(match1, unwind1, match2, unwind2, group, sort, project)).first();

        String mostCommonCategory = doc.getString("category");

        return mostCommonCategory;
    }

    /*
    db.Papers.aggregate([
      { $group: { _id: "$category", nPapers: {$sum: 1}}},
      { $project: {_id:0, category: "$_id", nPapers: 1}}
      ])
     */


    public List<Pair<String, Integer>> getCategoriesSummaryByLikes(/*String option*/) {

        List<Pair<String, Integer>> topCategories = new ArrayList<>();

        Bson group = group("$category", sum("nPapers", 1));
        Bson sort = sort(descending("nPapers"));
        Bson project = project(fields(computed("category", "$_id"),
                excludeId(), include("nPapers")));

        try(MongoCursor<Document> cursor = papersCollection.aggregate(Arrays.asList(group, sort, project))
                                    .iterator()) {
            while(cursor.hasNext()) {
                Document doc = cursor.next();
                String category = doc.getString("category");
                int nPapers = doc.getInteger("nPapers");
                topCategories.add(new Pair<>(category, nPapers));
            }
        }
        catch (Exception e) {
            System.out.println("Error in getting number of papers by category");
            e.printStackTrace();
        }

        return topCategories;
    }

    /* Create Paper */
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

    /*Read reading list(Da aggiungere)*/

    /*Update User(Da verificarne il funzionamento)*/
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

    public List<Paper> getPaperByCategory(String category){

        List<Paper> papers = new ArrayList<>();
        Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd hh:mm:ss").create();
        List<Document> results = (List<Document>) papersCollection.find(eq("category", category))
                .into(new ArrayList<>());
        Type paperListType = new TypeToken<ArrayList<Paper>>(){}.getType();
        papers = gson.fromJson(gson.toJson(results), paperListType);
        return papers;
    }

    /*Search Reading Lists by User(Da aggiungere)*/

    /*Weekly/Monthly/All time summary of the categories by number of papers Published */
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
