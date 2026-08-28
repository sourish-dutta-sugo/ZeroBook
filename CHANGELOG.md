# Changelog

All notable changes to this project will be documented in this file.

This project follows [Keep a Changelog](https://keepachangelog.com/en/1.1.0/).

## [2.2.3] - 2026-08-01

### Added
- Income vouchers now work as a full voucher type, mirroring Expense workflows for posting and reporting.
- Debit Note and Credit Note entries now post to ledger accounts with dedicated handling.

### Improved
- Party-related ledger entries now consistently populate partyId from the start.

### Fixed
- Financial-year lock checks now protect Expense and Income entries more reliably.
- Voucher reference handling now uses a dedicated column instead of fragile narration-based matching.
- Balance Sheet no longer force-balances itself, and the Trial Balance issue is corrected.
- Crashes when opening Journal, Credit Note, Debit Note, and Income are resolved.

## [Unreleased]

### Added
- Initial changelog file.
