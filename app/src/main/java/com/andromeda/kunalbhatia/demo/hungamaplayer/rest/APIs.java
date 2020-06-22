package com.andromeda.kunalbhatia.demo.hungamaplayer.rest;

import com.andromeda.kunalbhatia.demo.hungamaplayer.models.SignInResponse;
import com.andromeda.kunalbhatia.demo.hungamaplayer.models.CreatePostResponse;
import com.andromeda.kunalbhatia.demo.hungamaplayer.models.PostsResponse;

import java.util.Map;

import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.PartMap;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 *
 */
public interface APIs {


    @FormUrlEncoded
    @POST("auth_service/login/")
    Call<SignInResponse> login(@Field("email") String username, @Field("password") String password, @Field("device_key") String deviceKey);


    @GET("{post_service}/post/")
    Call<PostsResponse> getHomeFeedPosts(@Path("post_service") String postService, @Query("limit") String limit, @Query("offset") String offset,
                                         @Query("posts_from") String postsFrom,
                                         @Query("score") String score,
                                         @Query("measurement") String measurement);

    @Multipart
    @POST("social_service/post/")
    Call<CreatePostResponse> createPost(@PartMap Map<String, RequestBody> postBody);
}
