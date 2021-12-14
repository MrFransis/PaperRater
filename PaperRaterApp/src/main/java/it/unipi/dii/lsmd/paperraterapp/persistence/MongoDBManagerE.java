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
}
