package com.kratosgado.blog.backend.repositories.mongo;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.bson.Document;
import org.bson.types.ObjectId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import com.kratosgado.blog.dtos.response.CommentResponse.CommentWithoutUser;
import com.kratosgado.blog.enums.CommentStatus;
import com.kratosgado.blog.models.Comment;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Sorts;

@Repository
public class CommentRepository {

  private static final String COLLECTION = "comments";

  private final MongoCollection<Document> collection;

  public CommentRepository(MongoDatabase mongoDatabase) {
    this.collection = mongoDatabase.getCollection(COLLECTION);
  }

  public Comment save(Comment comment) {
    if (comment.getId() == null || comment.getId().isBlank()) {
      Document doc = toDocument(comment, null);
      collection.insertOne(doc);
      ObjectId id = doc.getObjectId("_id");
      if (id != null) {
        comment.setId(id.toHexString());
      }
      return comment;
    }

    ObjectId objectId = toObjectId(comment.getId());
    Document replacement = toDocument(comment, objectId);
    collection.replaceOne(Filters.eq("_id", objectId), replacement);
    return comment;
  }

  public Optional<Comment> findById(String id) {
    ObjectId objectId = tryObjectId(id);
    if (objectId == null) {
      return Optional.empty();
    }
    Document doc = collection.find(Filters.eq("_id", objectId)).first();
    return Optional.ofNullable(fromDocument(doc));
  }

  public void deleteById(String id) {
    ObjectId objectId = tryObjectId(id);
    if (objectId == null) {
      return;
    }
    collection.deleteOne(Filters.eq("_id", objectId));
  }

  public List<Comment> findAll() {
    List<Comment> out = new ArrayList<>();
    for (Document doc : collection.find()) {
      Comment c = fromDocument(doc);
      if (c != null) {
        out.add(c);
      }
    }
    return out;
  }

  public Page<Comment> findByPostId(Long postId, Pageable pageable) {
    var filter = Filters.eq("post_id", postId);
    return findComments(filter, pageable);
  }

  public Page<Comment> findByPostIdAndStatus(Long postId, CommentStatus status, Pageable pageable) {
    var filter = Filters.and(
        Filters.eq("post_id", postId),
        Filters.eq("status", status.name()));
    return findComments(filter, pageable);
  }

  public Page<CommentWithoutUser> findByUserId(Long userId, Pageable pageable) {
    var filter = Filters.eq("user_id", userId);
    long total = collection.countDocuments(filter);

    List<CommentWithoutUser> content = new ArrayList<>();
    for (Document doc : collection.find(filter)
        .sort(Sorts.descending("created_at"))
        .skip((int) pageable.getOffset())
        .limit(pageable.getPageSize())) {
      content.add(toCommentWithoutUser(doc));
    }

    return new PageImpl<>(content, pageable, total);
  }

  public Long countByPostIdAndStatus(Long postId, CommentStatus status) {
    var filter = Filters.and(
        Filters.eq("post_id", postId),
        Filters.eq("status", status.name()));
    return collection.countDocuments(filter);
  }

  private Page<Comment> findComments(org.bson.conversions.Bson filter, Pageable pageable) {
    long total = collection.countDocuments(filter);

    List<Comment> content = new ArrayList<>();
    for (Document doc : collection.find(filter)
        .sort(Sorts.descending("created_at"))
        .skip((int) pageable.getOffset())
        .limit(pageable.getPageSize())) {
      Comment c = fromDocument(doc);
      if (c != null) {
        content.add(c);
      }
    }

    return new PageImpl<>(content, pageable, total);
  }

  private static Document toDocument(Comment c, ObjectId id) {
    Document doc = new Document();
    if (id != null) {
      doc.put("_id", id);
    }
    doc.put("post_id", c.getPostId());
    doc.put("user_id", c.getUserId());
    doc.put("author_name", c.getAuthorName());
    doc.put("author_avatar_url", c.getAuthorAvatarUrl());
    doc.put("content", c.getContent());
    doc.put("status", c.getStatus() != null ? c.getStatus().name() : null);
    doc.put("created_at", toDate(c.getCreatedAt()));
    doc.put("updated_at", toDate(c.getUpdatedAt()));
    return doc;
  }

  private static Comment fromDocument(Document doc) {
    if (doc == null) {
      return null;
    }
    Comment c = new Comment();

    Object idVal = doc.get("_id");
    if (idVal instanceof ObjectId oid) {
      c.setId(oid.toHexString());
    } else if (idVal != null) {
      c.setId(String.valueOf(idVal));
    }

    c.setPostId(doc.getLong("post_id"));
    c.setUserId(doc.getLong("user_id"));
    c.setAuthorName(doc.getString("author_name"));
    c.setAuthorAvatarUrl(doc.getString("author_avatar_url"));
    c.setContent(doc.getString("content"));

    String status = doc.getString("status");
    if (status != null) {
      c.setStatus(CommentStatus.valueOf(status));
    }

    c.setCreatedAt(toLocalDateTime(doc.getDate("created_at")));
    c.setUpdatedAt(toLocalDateTime(doc.getDate("updated_at")));
    return c;
  }

  private static CommentWithoutUser toCommentWithoutUser(Document doc) {
    String id = null;
    Object idVal = doc.get("_id");
    if (idVal instanceof ObjectId oid) {
      id = oid.toHexString();
    } else if (idVal != null) {
      id = String.valueOf(idVal);
    }

    String status = doc.getString("status");
    CommentStatus cs = status != null ? CommentStatus.valueOf(status) : null;

    return new CommentWithoutUser(
        id,
        doc.getLong("post_id"),
        doc.getString("content"),
        cs,
        toLocalDateTime(doc.getDate("created_at")),
        toLocalDateTime(doc.getDate("updated_at")));
  }

  private static ObjectId toObjectId(String id) {
    return new ObjectId(id);
  }

  private static ObjectId tryObjectId(String id) {
    try {
      return new ObjectId(id);
    } catch (Exception e) {
      return null;
    }
  }

  // Manual pagination: approved comments for a post
  public List<Comment> findByPostIdAndStatusManual(Long postId, CommentStatus status, int size, int offset) {
    var filter = Filters.and(
        Filters.eq("post_id", postId),
        Filters.eq("status", status.name()));
    List<Comment> comments = new ArrayList<>();
    var find = collection.find(filter)
        .sort(Sorts.descending("created_at"))
        .skip(offset)
        .limit(size);
    for (Document doc : find) {
      Comment c = fromDocument(doc);
      if (c != null) {
        comments.add(c);
      }
    }
    return comments;
  }

  public List<Comment> findByPostIdManual(Long postId, int size, int offset) {
    var filter = Filters.eq("post_id", postId);
    List<Comment> comments = new ArrayList<>();
    var find = collection.find(filter)
        .sort(Sorts.descending("created_at"))
        .skip(offset)
        .limit(size);
    for (Document doc : find) {
      Comment c = fromDocument(doc);
      if (c != null) {
        comments.add(c);
      }
    }
    return comments;
  }

  public List<CommentWithoutUser> findCommentsByUserIdManual(Long userId, int size, int offset) {
    var filter = Filters.eq("user_id", userId);
    List<CommentWithoutUser> out = new ArrayList<>();
    for (Document doc : collection.find(filter).sort(Sorts.descending("created_at")).skip(offset).limit(size)) {
      out.add(toCommentWithoutUser(doc));
    }
    return out;
  }

  public long countByPostId(Long postId) {
    return collection.countDocuments(Filters.eq("post_id", postId));
  }

  public long countByUserId(Long userId) {
    return collection.countDocuments(Filters.eq("user_id", userId));
  }

  private static Date toDate(LocalDateTime ldt) {
    if (ldt == null) {
      return null;
    }
    return Date.from(ldt.atZone(ZoneId.systemDefault()).toInstant());
  }

  private static LocalDateTime toLocalDateTime(Date d) {
    if (d == null) {
      return null;
    }
    return LocalDateTime.ofInstant(d.toInstant(), ZoneId.systemDefault());
  }
}
