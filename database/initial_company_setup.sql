-- inserting company and inserting initial admin user for company
-- create extension pgcrypto;
DO $$
    DECLARE
        company_name TEXT := 'Company Name';
        company_domain TEXT := 'sample-company';
        company_id INT;
--         data of the initial admin user
        _username TEXT := 'username';
        _password TEXT := 'password';
        _first_name TEXT := 'First Name';
        _last_name TEXT := 'Last Name';
--        admin role corresponds to 0 ordinal, 1 - client, 2 - courier
        _role INT := 0;
        _hashed_password TEXT;
    BEGIN
        INSERT INTO "logistics-management-app".public.company (name, domain) VALUES (company_name, company_domain)
        RETURNING id INTO company_id;

--         to apply password hashing you must have pgcrypto extension installed, it can be done with command: 'create extension pgcrypto'
        _hashed_password := crypt(_password, gen_salt('bf'));
        INSERT INTO "logistics-management-app".public."user" (username, password, first_name, last_name, company, role)
        VALUES (_username, _hashed_password, _first_name, _last_name, company_id, _role);
END$$;
