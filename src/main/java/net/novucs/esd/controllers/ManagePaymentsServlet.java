package net.novucs.esd.controllers;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import net.novucs.esd.lifecycle.Session;
import net.novucs.esd.model.Payment;
import net.novucs.esd.model.User;
import net.novucs.esd.orm.Dao;
import net.novucs.esd.orm.Where;
import net.novucs.esd.util.PaginationUtil;

public class ManagePaymentsServlet extends BaseServlet {

  private static final long serialVersionUID = 1426082847044519303L;

  private static final String PAGE_SIZE_FILTER = "userPageSizeFilter";

  private static final String USER_SEARCH_QUERY = "userSearchQuery";

  @Inject
  private Dao<Payment> paymentDao;

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response)
      throws IOException, ServletException {

    try {
      Session session = Session.fromRequest(request);
      User user = session.getUser();
      String searchQuery = (String) session.getFilter(USER_SEARCH_QUERY);
      int pageSize = PaginationUtil.getPageSize(request, PAGE_SIZE_FILTER);
      double pageNumber = PaginationUtil.getPageNumber(request);

      List<Payment> payments;
      if (searchQuery == null) {
        payments = PaginationUtil
            .paginate(paymentDao, pageSize, pageNumber, new Where().eq("user_id", user.getId()));
      } else {
        String[] columns = {"name", "email"};
        payments = PaginationUtil.paginateWithSearch(paymentDao, pageSize, pageNumber,
            searchQuery, columns);
      }

      int max = PaginationUtil.getMaxPages(paymentDao, pageSize);
      PaginationUtil.setRequestAttributes(request, max, pageNumber, pageSize);
      request.setAttribute("payments", payments);
      session.removeFilter(USER_SEARCH_QUERY);

      super.forward(request, response, "Manage Payments", "user.payments.manage");
    } catch (SQLException e) {
      response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    }
  }

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response)
      throws IOException, ServletException {
    // Here we will set the filters in the users session.

    String searchQuery = request.getParameter("search-claims-query");
    PaginationUtil.postPaginationWithSearch(request, PAGE_SIZE_FILTER, USER_SEARCH_QUERY,
        searchQuery);

    response.sendRedirect("claims");
  }

  @Override
  public String getServletInfo() {
    return "ManagePaymentsServlet";
  }
}