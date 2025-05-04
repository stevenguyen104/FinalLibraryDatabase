package library.servlets;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import library.dao.OrderRequestsDAO;
import library.model.OrderRequest;
import library.util.DBUtil;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/manageRequestBook")
public class ManageRequestsServlet extends HttpServlet {
    private OrderRequestsDAO requestsDAO;

    @Override
    public void init() throws ServletException {
        super.init();
        requestsDAO = new OrderRequestsDAO();
    }
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try (Connection conn = DBUtil.getConnection()) {
            // Retrieve pending requests (adjust if you want all requests)
            List<OrderRequest> requestList = requestsDAO.getAllRequests(conn);
            request.setAttribute("requestList", requestList);
            request.getRequestDispatcher("/views/manageRequestBookView.jsp").forward(request, response);
        } catch (SQLException e) {
            throw new ServletException("Unable to load requests", e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String requestIdStr = request.getParameter("requestId");
        String newStatus = request.getParameter("newStatus");
        if (requestIdStr == null || requestIdStr.isEmpty()) {
            response.sendRedirect(request.getContextPath() + "/manageRequestBook");
            return;
        }
        int requestId = Integer.parseInt(requestIdStr);

        try {
            if ("Fulfilled".equalsIgnoreCase(newStatus)) {
                // Fulfilling the order will add the new book as available to the Books table
                try (Connection conn = DBUtil.getConnection()) {
                    requestsDAO.fulfillRequest(conn, requestId);
                } catch (SQLException e) {
                    throw e;
                }
            }
            response.sendRedirect(request.getContextPath() + "/manageRequestBook");
        } catch (SQLException e) {
            throw new ServletException("Error updating request", e);
        }
    }
}
