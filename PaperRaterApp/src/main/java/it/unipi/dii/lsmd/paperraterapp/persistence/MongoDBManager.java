package it.unipi.dii.lsmd.paperraterapp.persistence;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Aggregates;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Indexes;
import com.mongodb.client.model.Updates;
import com.mongodb.client.result.UpdateResult;
import it.unipi.dii.lsmd.paperraterapp.model.Comment;
import it.unipi.dii.lsmd.paperraterapp.model.Paper;
import it.unipi.dii.lsmd.paperraterapp.model.ReadingList;
import it.unipi.dii.lsmd.paperraterapp.model.User;
import javafx.util.Pair;
import org.bson.Document;
import org.bson.conversions.Bson;

import javax.xml.transform.Result;
import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Consumer;
import java.util.regex.Pattern;

import static com.mongodb.client.model.Accumulators.sum;
import static com.mongodb.client.model.Aggregates.*;
import static com.mongodb.client.model.Filters.*;
import static com.mongodb.client.model.Indexes.ascending;
import static com.mongodb.client.model.Projections.*;
import static com.mongodb.client.model.Sorts.descending;



public class MongoDBManager {
    public MongoDatabase db;
    private MongoCollection usersCollection;
    private MongoCollection papersCollection;


    /**
     *
     * @param client MongoDBClient
     */
    public MongoDBManager(MongoClient client) {
        this.db = client.getDatabase("PaperRater");
        usersCollection = db.getCollection("Users");
        papersCollection = db.getCollection("Papers");
    }
    //
    //public List<User> getUsersByKeyword (String keyword) {
    //
    //}

    /**
     *
     * @param username
     * @param password
     * @return
     */
    public User login (String username, String password) {
        Document result = (Document) usersCollection.find(Filters.and(eq("username", username),
                                                                eq("password", password))).
                                                                first();

        Gson gson = new Gson();
        return gson.fromJson(gson.toJson(result), User.class);
    }

    /**
     * Add a new User to MongoDB
     * @param u The object User which contains all the necessary information
     * @return true if operation is successfully executed, false otherwise
     */
    public boolean addUser (User u) {
        try {
            Document doc = new Document("username", u.getUsername())
                                .append("email", u.getEmail())
                                .append("password", u.getPassword());

            if (u.getFirstName() != null)
                doc.append("firstName", u.getFirstName());
            if (u.getLastName() != null)
                doc.append("lastName", u.getLastName());
            if (u.getPicture() != null)
                doc.append("picture", u.getPicture());
            if (u.getAge() != -1)
                doc.append("age", u.getAge());

            doc.append("readingLists", u.getReadingLists());

            usersCollection.insertOne(doc);
            return true;

        } catch (Exception e) {
            System.out.println("Error in adding a new user");
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Function that deletes the user from the database
     * @param u user to delete
     * @return true if operation is successfully executed, false otherwise
     */
    public boolean deleteUser(User u) {
        try {
            usersCollection.deleteOne(eq("username", u.getUsername()));
            return true;
        }
        catch (Exception ex)
        {
            System.err.println("Error in delete user");
            return false;
        }
    }

    /**
     * Edit an already present user
     * @param u the new user information to replace the old one
     * @return  true if operation is successfully executed, false otherwise
     */
    public boolean updateUser (User u){
        try {
            Document doc = new Document().append("username", u.getUsername());
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

            Bson updateOperation = new Document("$set", doc);
            usersCollection.updateOne(new Document("username", u.getUsername()), updateOperation);
            return true;
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
            System.err.println("Error in updating user on MongoDB");
            return false;
        }
    }

    /**
     * Search a user by his username
     * @param username username of the user
     * @return User
     */
    public User getUserByUsername (String username) {
        Document result = (Document) usersCollection.find((eq("username", username))).first();
        if (result == null) {
            System.out.println("User " + username + " do not found.");
            return null;
        }

        Gson gson = new Gson();
        return gson.fromJson(gson.toJson(result), User.class);
    }

    /**
     * Add a new paper in MongoDB
     * @param p The object Paper which contains all the necessary information about it
     * @return  true if operation is successfully executed, false otherwise
     */
    public boolean addPaper (Paper p) {
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

    /**
     * Add a new comment
     * @param id Id of the paper
     * @param text text of the comment
     * @return  true if operation is successfully executed, false otherwise
     */
    public boolean addComment (String id, String text, String user) {
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Document comment = new Document("username", user)
                    .append("text",text)
                    .append("timestamp",dateFormat.format(new Date()));

            Bson find = or(eq("arxiv_id", id), eq("vixra_id", id));
            Bson update = Updates.addToSet("comments", comment);
            papersCollection.updateOne(find, update);
            return true;
        }
        catch (Exception e)
        {
            System.out.println("Error in adding a commnet to a Paper");
            e.printStackTrace();
            return false;
        }
    }
    /**
     * Update the list of comments of a paper
     * @param paper
     * @param comment
     * @return  true if operation is successfully executed, false otherwise
     */
    public boolean updateComments(Paper p, List<Comment> comments){
        try{
            Bson update = new Document("comments", comments);
            Bson updateOperation = new Document("$set", update);
            if(!p.getArxiv_id().isEmpty())
                papersCollection.updateOne(new Document("arxiv_id", p.getArxiv_id()), updateOperation);
            else
                papersCollection.updateOne(new Document("vixra_id", p.getVixra_id()), updateOperation);
            return true;
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
            System.err.println("Error in updating user on MongoDB");
            return false;
        }
    }

    /**
     * Update an existing comment
     * @param paper
     * @param comment
     */
    public void updateComment(Paper paper, Comment comment){
        List<Comment> comments = paper.getComments();
        int i=0;
        for (Comment c: comments
        ) {
            if(c.getUsername().equals(comment.getUsername()) && c.getTimestamp().equals(
                    comment.getTimestamp())){
                comments.set(i, comment);
                break;
            }
            i++;
        }
        updateComments(paper, comments);
    }

    /**
     * Add a new comment
     * @param paper
     * @param comment
     */
    public void deleteComment (Paper paper, Comment comment) {
        List<Comment> comments = paper.getComments();
        int n = 0;
        int d = 0;
        for (Comment c : comments){
            if (c.getTimestamp().equals(comment.getTimestamp()) && c.getUsername().equals(comment.getUsername())) {
                d = n;
                break;
            }
        n++;
        }
        comments.remove(d);
        updateComments(paper, comments);
    }
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

    /**
     * Function that return the paper that matches the id
     * @param id of the paper to retrieve
     * @return the paper object
     */
    public Paper getPaperById (String id) {
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

    /**
     *
     * @param title
     * @param author
     * @param start_date
     * @param end_date
     * @param category
     * @return
     */
    public List<Paper> searchPapersByParameters (String title, String author, String start_date,
                                                 String end_date, String category, int skip, int limit) {
        List<Paper> papers = new ArrayList<>();
        Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd hh:mm:ss").create();

        List<Bson> pipeline = new ArrayList<>();

        if (!title.isEmpty()) {
            Pattern pattern1 = Pattern.compile("^.*" + title + ".*$", Pattern.CASE_INSENSITIVE);
            pipeline.add(Aggregates.match(Filters.regex("title", pattern1)));
        }

        if (!author.isEmpty()) {
            Pattern pattern2 = Pattern.compile("^.*" + author + ".*$", Pattern.CASE_INSENSITIVE);
            pipeline.add(Aggregates.match(Filters.regex("authors", pattern2)));
        }

        if (!start_date.isEmpty()) {
            pipeline.add(Aggregates.match(and(Filters.gte("published", start_date))));
        }

        if(!end_date.isEmpty()) {
            pipeline.add(Aggregates.match(and(Filters.lte("published", end_date))));
        }

        if (!category.isEmpty()) {
            pipeline.add(Aggregates.match(Filters.eq("category", category)));
        }

        pipeline.add(sort(descending("published")));
        pipeline.add(skip(skip));
        pipeline.add(limit(limit));

        List<Document> results = (List<Document>) papersCollection.aggregate(pipeline)
                .into(new ArrayList<>());
        Type papersListType = new TypeToken<ArrayList<Paper>>(){}.getType();
        papers = gson.fromJson(gson.toJson(results), papersListType);
        return papers;
    }

    /**
     * Function that return the list of papers that partially match a title
     * @param title title of the papers
     * @return The list of papers
     */
    public List<Paper> searchPapersByTitle(String title) {
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

    /**
     * Function that retrieves all the papers published by an author
     * @param author name of the author
     * @return list of Papers
     */
    public List<Paper> searchPaperByAuthor(String author) {
        Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd").serializeSpecialFloatingPointValues().create();
        List<Paper> results = new ArrayList<>();
        Consumer<Document> transformDocument = doc -> {
            Paper paper = gson.fromJson(gson.toJson(doc), Paper.class);
            results.add(paper);
        };
        papersCollection.find(eq("authors", author)).forEach(transformDocument);
        return results;
    }

    /**
     * Return all the papers between a given time interval
     * @param start_date start date
     * @param end_date end date
     * @return the list of Papers
     */
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

    /**
     * Function that return the list of papers related to a category
     * @param category Category of the papers
     * @return The list of papers
     */
    public List<Paper> searchPapersByCategory(String category){

        List<Paper> papers = new ArrayList<>();
        Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd hh:mm:ss").create();
        List<Document> results = (List<Document>) papersCollection.find(eq("category", category))
                .into(new ArrayList<>());
        Type paperListType = new TypeToken<ArrayList<Paper>>(){}.getType();
        papers = gson.fromJson(gson.toJson(results), paperListType);
        return papers;
    }

    /**
     * Add a new empty reading list called "title" list at the user identify by username
     * @param username username of the user
     * @param title title of the new reading list
     * @return true if it adds the reading list, otherwise it returns false
     */
    public boolean createReadingList(String username, String title) {
        // check if there are other list with the same name
        Document document = (Document) usersCollection.find(and(eq("username", username),
                eq("readingLists.title", title))).first();
        if (document != null) {
            System.err.println("ERROR: name already in use.");
            return false;
        }
        // create the new reading_list
        Document readingList = new Document("title", title)
                .append("papers", Arrays.asList());
        // insert the new reading_list
        usersCollection.updateOne(
                eq("username", username),
                new Document().append(
                        "$push",
                        new Document("readingLists", readingList)
                )
        );
        System.out.println("Reading list " + title + " has been added");
        return true;
    }

    /**
     * Delete the reading list of user by specifying the title
     * @param username username of the user
     * @param title title of the reading list which i want remove
     * @return true if it removes the reading list, otherwise it returns false
     */
    public boolean deleteReadingList(String username, String title){
        Bson filter = new Document().append("username", username);
        Bson fields = new Document().append("reading_lists", new Document("title", title));
        Bson update = new Document("$pull", fields);
        UpdateResult updateResult = usersCollection.updateOne(filter, update);
        if (updateResult.getModifiedCount() == 0){
            System.err.println("ERROR: can not delete the reading list " + title);
            return false;
        } else {
            System.out.println("Reading list " + title + " has been deleted");
            return true;
        }
    }

    /**
     * Method that adds a Paper to a ReadingList
     * @param user owner of the ReadingList
     * @param p Paper
     * @return true if the operation is successfully executed, false otherwise
     */
    public UpdateResult addPaperToReadingList(String user, String title, Paper p) {
        Document paperReduced = new Document("arxiv_id", p.getArxiv_id())
                .append("vixra_id", p.getVixra_id())
                .append("title", p.getTitle())
                .append("auhtors", p.getAuthors())
                .append("category", p.getCategory());
        Bson find = and(eq("username", user),
                eq("readingLists.title", title));
        Bson update = Updates.addToSet("readingLists.$.papers", paperReduced);
        UpdateResult result = usersCollection.updateOne(find, update);
        return result;
    }

    /**
     * Method that remove a Paper from a ReadingList
     * @param user ReadingList
     * @param p Paper
     * @return true if the operation is successfully executed, false otherwise
     */
    public boolean removePaperFromReadingList(String user, String title, Paper p) {
        try {
            Document paperReduced = new Document("arxiv_id", p.getArxiv_id())
                    .append("vixra_id", p.getVixra_id())
                    .append("title", p.getTitle())
                    .append("auhtors", p.getAuthors());

            Bson find = and(eq("username", user),
                    eq("reading_lists.title", title));
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
        Bson project = project(fields(excludeId() ,computed("ReadingList", "$reading_lists")));
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

    /**
     * Function that return the ReadingLists given the user and the name
     * @param username Username of the user
     * @param title Name of the reading list
     * @return  The list of reading lists
     */
    public ReadingList getReadingList (String username, String title) {
        ReadingList readingList = null;
        Gson gson = new GsonBuilder().serializeNulls().create();

        Bson match = match(eq("username", username));
        Bson unwind = unwind("$reading_lists");
        Bson match2 = match(eq("reading_lists.title", title));
        Bson project = project(fields(excludeId(), computed("ReadingList", "$reading_lists")));
        MongoCursor<Document> iterator = (MongoCursor<Document>) usersCollection.aggregate(Arrays.asList(match, unwind, match2, project)).iterator();
        if(iterator.hasNext()){
            Document document = iterator.next();
            Document ReadingListDocument = (Document) document.get("ReadingList");
            readingList = gson.fromJson(gson.toJson(ReadingListDocument), ReadingList.class);
        }
        return readingList;
    }

    /**
     * Function that returns the most common category in a Reading List
     * @param user Reading List to process
     * @return the name of the category
     */
    public String getMostCommonCategoryInReadingList(String user, String title) {

        Bson match1 = match(Filters.eq("username", user));
        Bson unwind1 = unwind("$reading_lists");
        Bson match2 = match(Filters.eq("reading_lists.title", title));
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


    /**
     *
     * @return
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



    /**
     * Braws all comments that has been written "numDays" ago
     * @param numDays how may day i want to scan
     * @return list of comments
     */
    public List<Comment> searchLastComments(int numDays) {
        // create the date
        LocalDateTime localDateTime = LocalDateTime.now().minusDays(numDays);
        LocalDateTime startOfDay = localDateTime.toLocalDate().atStartOfDay();
        String filterDate = startOfDay.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").serializeSpecialFloatingPointValues().create();
        List<Comment> results = new ArrayList<>();
        Consumer<Document> takeComments = doc -> {
            Document docComments = (Document) doc.get("comments");
            Comment comment = gson.fromJson(gson.toJson(docComments), Comment.class);
            results.add(comment);
        };

        Bson unwind = unwind("$comments");
        Bson filter = match(gte("comments.timestamp", filterDate));
        Bson sort = sort(ascending("comments.timestamp"));
        papersCollection.aggregate(Arrays.asList(unwind, filter, sort)).forEach(takeComments);

        return results;
    }

    /**
     * Browse the top categories with more comments
     * @param period (all, month, week)
     * @param top (positive integer)
     * @return HashMap with the category and the number of comments
     */
    public HashMap<String, Integer> summaryCategoriesByComments(String period, int top) {
        LocalDateTime localDateTime = LocalDateTime.now();
        LocalDateTime startOfDay;
        switch (period) {
            case "all" -> startOfDay = LocalDateTime.MIN;
            case "month" -> startOfDay = localDateTime.toLocalDate().atStartOfDay().minusMonths(1);
            case "week" -> startOfDay = localDateTime.toLocalDate().atStartOfDay().minusWeeks(1);
            default -> {
                System.err.println("ERROR: Wrong period.");
                return null;
            }
        }
        String filterDate = startOfDay.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        HashMap<String, Integer> results = new HashMap<>();
        Consumer<Document> rankCategories = doc ->
                results.put((String) doc.get("_id"), (Integer) doc.get("tots"));

        Bson unwind = unwind("$comments");
        Bson filter = match(gte("comments.timestamp", filterDate));
        Bson group = group("$category", sum("tots", 1));
        Bson sort = sort(Indexes.descending("tots"));
        Bson limit = limit(top);
        papersCollection.aggregate(Arrays.asList(unwind, filter, group, sort, limit)).forEach(rankCategories);

        return results;
    }

    /**
     * Browse the top papers with more comments
     * @param period (all, month, week)
     * @param top (positive integer)
     * @return HashMap with the title and the number of comments
     */
    public HashMap<String, Integer> summaryPapersByComments(String period, int top) {
        LocalDateTime localDateTime = LocalDateTime.now();
        LocalDateTime startOfDay;
        switch (period) {
            case "all" -> startOfDay = LocalDateTime.MIN;
            case "month" -> startOfDay = localDateTime.toLocalDate().atStartOfDay().minusMonths(1);
            case "week" -> startOfDay = localDateTime.toLocalDate().atStartOfDay().minusWeeks(1);
            default -> {
                System.err.println("ERROR: Wrong period.");
                return null;
            }
        }
        String filterDate = startOfDay.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        HashMap<String, Integer> results = new HashMap<>();
        Consumer<Document> rankPapers = doc ->
                results.put((String) doc.get("_id"), (Integer) doc.get("tots"));

        Bson unwind = unwind("$comments");
        Bson filter = match(gte("comments.timestamp", filterDate));
        Bson group = group("$title", sum("tots", 1));
        Bson sort = sort(Indexes.descending("tots"));
        Bson limit = limit(top);
        papersCollection.aggregate(Arrays.asList(unwind, filter, group, sort, limit)).forEach(rankPapers);

        return results;
    }

    public List<String> getCategories() {
        List<String> categoriesList = new ArrayList<>();
        papersCollection.distinct("category", String.class).into(categoriesList);
        return categoriesList;
    }
}
