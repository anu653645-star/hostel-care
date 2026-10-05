package edu.citchennai.hostel;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:hostel-test;DB_CLOSE_DELAY=-1",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
@AutoConfigureMockMvc
class WorkflowIntegrationTest {
    @Autowired
    private MockMvc mvc;

    @Test
    void studentComplaintIsReviewedScheduledAndConfirmedSolved() throws Exception {
        mvc.perform(get("/register"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Create your account")));

        register("Student", "student@citchennai.net", "STUDENT", "A Block", "204");
        MockHttpSession studentSession = login("student@citchennai.net");

        mvc.perform(get("/").session(studentSession))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Raise a complaint")));

        MockMultipartFile photo = new MockMultipartFile(
                "photo", "fan.jpg", "image/jpeg", new byte[]{(byte) 0xff, (byte) 0xd8, (byte) 0xff});
        mvc.perform(multipart("/complaints").file(photo)
                        .param("category", "FAN")
                        .param("description", "The fan is making a loud noise")
                        .with(csrf())
                        .session(studentSession))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/?submitted=1"));

        register("Warden", "warden@citchennai.net", "WARDEN", "", "");
        MockHttpSession wardenSession = login("warden@citchennai.net");
        mvc.perform(get("/").session(wardenSession))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("The fan is making a loud noise")));
        mvc.perform(post("/complaints/1/accept").with(csrf()).session(wardenSession))
                .andExpect(status().is3xxRedirection());

        register("Hostel Aunty", "aunty@citchennai.net", "HOSTEL_AUNTY", "", "");
        MockHttpSession auntySession = login("aunty@citchennai.net");
        mvc.perform(get("/").session(auntySession))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Student · A Block · Room 204")));
        mvc.perform(post("/complaints/1/schedule")
                        .param("visitDate", LocalDate.now().plusDays(1).toString())
                        .with(csrf())
                        .session(auntySession))
                .andExpect(status().is3xxRedirection());

        mvc.perform(get("/").session(studentSession))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Mark as solved")));
        mvc.perform(post("/complaints/1/solved").with(csrf()).session(studentSession))
                .andExpect(status().is3xxRedirection());

        mvc.perform(get("/").session(wardenSession))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Problem solved")));
    }

    private void register(String name, String email, String role, String hostel, String room) throws Exception {
        mvc.perform(post("/register")
                        .param("fullName", name)
                        .param("email", email)
                        .param("password", "password123")
                        .param("role", role)
                        .param("hostel", hostel)
                        .param("roomNumber", room)
                                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?registered"));
    }

    private MockHttpSession login(String email) throws Exception {
        MvcResult result = mvc.perform(post("/login")
                        .param("username", email)
                        .param("password", "password123")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"))
                .andReturn();
        return (MockHttpSession) result.getRequest().getSession(false);
    }
}
