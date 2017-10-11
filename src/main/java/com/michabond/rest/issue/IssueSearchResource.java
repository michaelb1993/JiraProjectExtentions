package com.michabond.rest.issue;


import com.atlassian.jira.bc.issue.search.SearchService;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.search.SearchException;
import com.atlassian.jira.issue.search.SearchResults;
import com.atlassian.jira.jql.builder.JqlClauseBuilder;
import com.atlassian.jira.jql.builder.JqlQueryBuilder;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.web.bean.PagerFilter;
import com.atlassian.plugins.rest.common.security.AnonymousAllowed;
import com.atlassian.query.Query;

import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;

@Named
@Path("/issue")
public class IssueSearchResource {

    private SearchService issueSearchService;

    @Inject
    public IssueSearchResource(SearchService issueSearchService) {
        this.issueSearchService = issueSearchService;
    }

    @GET
    @Path("/health")
    @Produces({MediaType.APPLICATION_JSON})
    @AnonymousAllowed
    public Response health() {
        return Response.ok("ok").build();
    }

    /**
     * Call from select2 JS plugin
     * Response needs to look like this: [{ 'id': 1, 'text': 'Demo' }, { 'id': 2, 'text': 'Demo 2'}]
     */
    @GET
    @Path("/{projectKey}/search")
    @Produces({MediaType.APPLICATION_JSON})
    public Response searchIssues(@PathParam("projectKey") final String projectKey,
                                 @QueryParam("query") final String issueQuery,
                                 @Context HttpServletRequest request) {
        // User is required to carry out a search
        ApplicationUser applicationUser = null;
        if (request != null) {
            JiraAuthenticationContext authContext = ComponentAccessor.getJiraAuthenticationContext();
            applicationUser = authContext.getLoggedInUser();
        }

        // search issues

        // The search interface requires JQL clause... so let's build one
        JqlClauseBuilder jqlClauseBuilder = JqlQueryBuilder.newClauseBuilder();
        // Our JQL clause is simple project="TUTORIAL"
        Query query = jqlClauseBuilder.project(projectKey).and().summary(issueQuery + "*").buildQuery();
        // A page filter is used to provide pagination. Let's use an unlimited filter to
        // to bypass pagination.
        PagerFilter pagerFilter = PagerFilter.getUnlimitedFilter();
        SearchResults searchResults = null;
        try {
            // Perform search results
            searchResults = this.issueSearchService.search(applicationUser, query, pagerFilter);
        } catch (SearchException e) {
            e.printStackTrace();
        }
        // return the results
        List<Issue> resultIssues = searchResults.getIssues();
        return Response.ok(serializeIssues(resultIssues)).build();
    }

    private List<IssueSearchResourceModel> serializeIssues(List<Issue> issues) {
        List<IssueSearchResourceModel> issueModels = new ArrayList<IssueSearchResourceModel>();
        for (Issue issue : issues) {
            issueModels.add(new IssueSearchResourceModel(issue));
        }
        return issueModels;
    }

}
