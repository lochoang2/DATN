package poly.edu.duantotnghiep_nhom2.controller;

import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import poly.edu.duantotnghiep_nhom2.entity.User;
import poly.edu.duantotnghiep_nhom2.repository.PitchRepository;
import poly.edu.duantotnghiep_nhom2.service.ReviewService;
import poly.edu.duantotnghiep_nhom2.service.UserService;

@Controller
public class HomeController {

    private final PitchRepository pitchRepository;
    private final ReviewService reviewService;
    private final UserService userService;

    public HomeController(PitchRepository pitchRepository, ReviewService reviewService, UserService userService) {
        this.pitchRepository = pitchRepository;
        this.reviewService = reviewService;
        this.userService = userService;
    }

    // Map cả "/" và "/home" về cùng một trang
    @GetMapping({"/", "/home"})
    public String home(Model model, Authentication authentication) {
        // KIỂM TRA QUYỀN ADMIN ĐỂ REDIRECT
        if (authentication != null && authentication.isAuthenticated()) {
            boolean isAdmin = authentication.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ADMIN") || a.getAuthority().equals("ROLE_ADMIN"));
            if (isAdmin) {
                return "redirect:/admin/dashboard";
            }
        }

        try {
            // Lấy 6 sân được đặt nhiều nhất
            model.addAttribute("featuredPitches", pitchRepository.findTopPitchesByBookings(PageRequest.of(0, 6)));
            
            // Lấy đánh giá mới nhất
            model.addAttribute("latestReviews", reviewService.getLatestReviews());
        } catch (Exception e) {
            e.printStackTrace(); 
        }
        
        return "home";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/register")
    public String register(Model model) {
        model.addAttribute("user", new User());
        return "register";
    }

    // Xử lý Đăng ký
    @PostMapping("/register")
    public String processRegister(@ModelAttribute("user") User user, RedirectAttributes redirectAttributes) {
        try {
            userService.register(user);
            redirectAttributes.addFlashAttribute("success", "Đăng ký thành công! Vui lòng đăng nhập.");
            return "redirect:/login";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/register"; 
        }
    }
}
