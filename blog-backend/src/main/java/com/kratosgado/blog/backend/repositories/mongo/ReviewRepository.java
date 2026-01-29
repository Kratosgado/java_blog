package com.kratosgado.blog.backend.repositories.mongo;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import com.kratosgado.blog.dtos.response.ReviewResponse;
import com.kratosgado.blog.models.Review;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Accumulators;
import com.mongodb.client.model.Aggregates;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Sorts;

@Repository
public class ReviewRepository {

  private static final String COLLECTION = "reviews";
  private final MongoCollection<Document> collection;

  public ReviewRepository(MongoDatabase mongoDatabase) {
    this.collection = mongoDatabase.getCollection(COLLECTION);
  }

  public Review save(Review review) {
    if (review.getId() == null || review.getId().isBlank()) {
      Document doc = toDocument(review, null);
      collection.insertOne(doc);
      ObjectId id = doc.getObjectId("_id");
      if (id != null) {
        review.setId(id.toHexString());
      }
      return review;
    }

    ObjectId objectId = toObjectId(review.getId());
    Document replacement = toDocument(review, objectId);
    collection.replaceOne(Filters.eq("_id", objectId), replacement);
    return review;
  }

  public Optional<Review> findById(String id) {
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

  public List<Review> findAll() {
    List<Review> out = new ArrayList<>();
    for (Document doc : collection.find()) {
      Review r = fromDocument(doc);
      if (r != null) {
        out.add(r);
      }
    }
    return out;
  }

  public Page<Review> findByPostId(Long postId, Pageable pageable) {
    var filter = Filters.eq("post_id", postId);
    return findReviews(filter, pageable, false);
  }

  public List<Review> findByPostIdOrderByCreatedAtDesc(Long postId, int size, int offset) {
    List<Review> reviews = new ArrayList<>();
    var filter = Filters.eq("post_id", postId);
    var find = collection.find(filter)
      .sort(Sorts.descending("created_at"))
      .skip(offset)
      .limit(size);
    for (Document doc : find) {
      Review r = fromDocument(doc);
      if (r != null) {
        reviews.add(r);
      }
    }
    return reviews;
  }

  public long countByPostId(Long postId) {
    return collection.countDocuments(Filters.eq("post_id", postId));
  }

  public Page<Review> findByPostIdOrderByCreatedAtDesc(Long postId, Pageable pageable) {
    var filter = Filters.eq("post_id", postId);
    return findReviews(filter, pageable, true);
  }

  public List<ReviewResponse.ReviewWithoutUser> findByUserId(Long userId, int size, int offset) {
    var filter = Filters.eq("user_id", userId);
    List<ReviewResponse.ReviewWithoutUser> content = new ArrayList<>();
    for (Document doc : collection.find(filter)
        .sort(Sorts.descending("created_at"))
        .skip(offset)
        .limit(size)) {
      content.add(toReviewWithoutUser(doc));
    }
    return content;
  }

  public long countByUserId(Long userId) {
    return collection.countDocuments(Filters.eq("user_id", userId));
  }

  public long countAll() {
    return collection.countDocuments();
  }

  public Page<ReviewResponse.ReviewWithoutUser> findByUserId(Long userId, Pageable pageable) {
    var filter = Filters.eq("user_id", userId);
    long total = collection.countDocuments(filter);

    List<ReviewResponse.ReviewWithoutUser> content = new ArrayList<>();
    for (Document doc : collection.find(filter)
        .sort(Sorts.descending("created_at"))
        .skip((int) pageable.getOffset())
        .limit(pageable.getPageSize())) {
      content.add(toReviewWithoutUser(doc));
    }

    return new PageImpl<>(content, pageable, total);
  }

  public List<ReviewResponse.AverageRatingResult> getAverageRatingByPostId(Long postId) {
    List<Bson> pipeline = List.of(
        Aggregates.match(Filters.eq("post_id", postId)),
        Aggregates.group(null, Accumulators.avg("avgRating", "$rating")));

    List<ReviewResponse.AverageRatingResult> out = new ArrayList<>();
    for (Document doc : collection.aggregate(pipeline)) {
      Object avg = doc.get("avgRating");
      Double avgDouble = null;
      if (avg instanceof Number n) {
        avgDouble = n.doubleValue();
      }
      out.add(new ReviewResponse.AverageRatingResult(avgDouble));
    }
    return out;
  }

  // DEPRECATED: See new primitive return below
  // DEPRECATED: Use primitive long version for consistency with refactor.
  // public Long countByPostId(Long postId) {
  //   return collection.countDocuments(Filters.eq("post_id", postId));
  // }

  public Optional<Review> findByPostIdAndUserId(Long postId, Long userId) {
    var filter = Filters.and(
        Filters.eq("post_id", postId),
        Filters.eq("user_id", userId));
    Document doc = collection.find(filter).first();
    return Optional.ofNullable(fromDocument(doc));
  }

  public boolean existsByPostIdAndUserId(Long postId, Long userId) {
    return findByPostIdAndUserId(postId, userId).isPresent();
  }

  private Page<Review> findReviews(Bson filter, Pageable pageable, boolean sortByCreatedAtDesc) {
    long total = collection.countDocuments(filter);

    List<Review> content = new ArrayList<>();
    var find = collection.find(filter)
        .skip((int) pageable.getOffset())
        .limit(pageable.getPageSize());

    if (sortByCreatedAtDesc) {
      find = find.sort(Sorts.descending("created_at"));
    }

    for (Document doc : find) {
      Review r = fromDocument(doc);
      if (r != null) {
        content.add(r);
      }
    }

    return new PageImpl<>(content, pageable, total);
  }

  private static Document toDocument(Review r, ObjectId id) {
    Document doc = new Document();
    if (id != null) {
      doc.put("_id", id);
    }
    doc.put("post_id", r.getPostId());
    doc.put("user_id", r.getUserId());
    doc.put("author_name", r.getAuthorName());
    doc.put("author_avatar_url", r.getAuthorAvatarUrl());
    doc.put("rating", r.getRating());
    doc.put("title", r.getTitle());
    doc.put("content", r.getContent());
    doc.put("created_at", toDate(r.getCreatedAt()));
    doc.put("updated_at", toDate(r.getUpdatedAt()));
    doc.put("helpful", r.isHelpful());
    return doc;
  }

  private static Review fromDocument(Document doc) {
    if (doc == null) {
      return null;
    }
    Review r = new Review();

    Object idVal = doc.get("_id");
    if (idVal instanceof ObjectId oid) {
      r.setId(oid.toHexString());
    } else if (idVal != null) {
      r.setId(String.valueOf(idVal));
    }

    r.setPostId(doc.getLong("post_id"));
    r.setUserId(doc.getLong("user_id"));
    r.setAuthorName(doc.getString("author_name"));
    r.setAuthorAvatarUrl(doc.getString("author_avatar_url"));

    Integer rating = doc.getInteger("rating");
    if (rating != null) {
      r.setRating(rating);
    }

    r.setTitle(doc.getString("title"));
    r.setContent(doc.getString("content"));

    r.setCreatedAt(toLocalDateTime(doc.getDate("created_at")));
    r.setUpdatedAt(toLocalDateTime(doc.getDate("updated_at")));

    Boolean helpful = doc.getBoolean("helpful");
    if (helpful != null) {
      r.setHelpful(helpful);
    }

    return r;
  }

  private static ReviewResponse.ReviewWithoutUser toReviewWithoutUser(Document doc) {
    String id = null;
    Object idVal = doc.get("_id");
    if (idVal instanceof ObjectId oid) {
      id = oid.toHexString();
    } else if (idVal != null) {
      id = String.valueOf(idVal);
    }

    Integer rating = doc.getInteger("rating");
    int ratingVal = rating != null ? rating : 0;

    return new ReviewResponse.ReviewWithoutUser(
        id,
        doc.getLong("post_id"),
        ratingVal,
        doc.getString("title"),
        doc.getString("content"),
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
