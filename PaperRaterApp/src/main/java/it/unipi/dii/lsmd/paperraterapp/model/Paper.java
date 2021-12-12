package it.unipi.dii.lsmd.paperraterapp.model;
import java.util.Date;
import java.util.List;

public class Paper {
    private String arxiv_id;
    private String vixra_id;
    private String title;
    private String _abstract;
    private String category;
    private List<String> authors;
    private Date published;
    private List<Comment> comments;

    public Paper(String arxiv_id, String vixra_id, String title, String _abstract, String category, List<String> authors, Date published, List<Comment> comments)
    {
        this.arxiv_id = arxiv_id;
        this.vixra_id = vixra_id;
        this.title = title;
        this._abstract = _abstract;
        this.category = category;
        this.authors = authors;
        this.published = published;
        this.comments = comments;
    }

    public String getArxiv_id() {
        return arxiv_id;
    }

    public String getVixra_id() {
        return vixra_id;
    }

    public String getTitle() {
        return title;
    }

    public String getAbstract() { return _abstract; }

    public String getCategory() {
        return category;
    }

    public List<String> getAuthors() {
        return authors;
    }

    public Date getPublished() {
        return published;
    }

    public List<Comment> getComments() {
        return comments;
    }

    public void setArxiv_id(String arxiv_id) { this.arxiv_id = arxiv_id; }

    public void setVixra_id(String vixra_id) { this.vixra_id = vixra_id; }

    public void setTitle(String title) { this.title = title; }

    public void setAbstract(String _abstract) { this._abstract = _abstract; }

    public void setCategory(String category) { this.category = category; }

    public void setAuthors(List<String> authors) { this.authors = authors; }

    public void setPublished(Date published) { this.published = published; }

    public void setComments(List<Comment> comments) { this.comments = comments; }

    @Override
    public String toString() {
        return "Paper{" +
                "title='" + title + '\'' +
                ", abstract='" + _abstract + '\'' +
                ", category=" + category +
                ", authors=" + authors +
                ", published=" + published +
                ", comments=" + comments +
                '}';
    }
}
